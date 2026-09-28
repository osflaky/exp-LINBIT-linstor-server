#!/usr/bin/env python3
"""Make the LINSTOR controller highly available (DRBD Reactor promoter).

Mirrors the LINSTOR user guide section "Creating a highly available
LINSTOR cluster". Ships with linstor-controller.

Run this script AS ROOT on the node that is CURRENTLY running
linstor-controller. It will:

  1. create a dedicated DRBD resource (`linstor_db`) for the controller DB
  2. move /var/lib/linstor onto that resource
  3. hand controller start/stop over to drbd-reactor (promoter)
  4. generate the script to run on every standby controller node

Everything is discovered from the live cluster. --storage-pool and --nodes
only pin down what would otherwise be auto-detected or asked for, so the
whole run can be described by one copy-pasteable command line (this is what
the LINSTOR GUI builds for you).

Requirements on this node: linstor-client (the script talks to the local
controller through its python-linstor library), drbd-utils, drbd-reactor.
Standby nodes need linstor-controller and drbd-reactor installed as well.
"""

import argparse
import importlib
import json
import os
import shutil
import socket
import subprocess
import sys
import time

RESOURCE = "linstor_db"
RESOURCE_GROUP = "ha-grp"
DEFAULT_DB_SIZE = "200M"

MOUNT_UNIT_PATH = "/etc/systemd/system/var-lib-linstor.mount"
REACTOR_CONF_PATH = "/etc/drbd-reactor.d/linstor_db.toml"
CLIENT_CONF_PATH = "/etc/linstor/linstor-client.conf"

MOUNT_UNIT = """[Unit]
Description=Filesystem for the LINSTOR controller

[Mount]
What=/dev/drbd/by-res/linstor_db/0
Where=/var/lib/linstor
"""


def reactor_conf(vip=None):
    """The promoter snippet. With a VIP, an ocf:heartbeat:IPaddr2 agent is
    started between the mount and the controller, so the address follows the
    active controller — the user guide's alternative to pointing every client
    at a list of controller IPs."""
    services = ['"var-lib-linstor.mount"']
    if vip:
        address, prefix = vip
        services.append('"ocf:heartbeat:IPaddr2 service_ip ip=' + address
                        + ' cidr_netmask=' + str(prefix) + '"')
    services.append('"linstor-controller.service"')
    return ("""[[promoter]]
[promoter.resources.linstor_db]
start = [
  """ + ",\n  ".join(services) + """,
]
""")



def parse_args(argv=None):
    parser = argparse.ArgumentParser(
        description="Make the LINSTOR controller highly available by moving "
                    "its database onto a replicated DRBD resource and "
                    "handing the service over to drbd-reactor.")
    parser.add_argument(
        "--storage-pool", metavar="NAME", default="",
        help="storage pool backing the controller database. Default: the "
             "cluster's only storage pool, or an interactive choice.")
    parser.add_argument(
        "--nodes", metavar="NODE[,NODE...]", default="",
        help="controller candidate nodes. The database resource is placed on "
             "exactly these nodes and only they get a controller; the list "
             "must include the node this script runs on. Default: let LINSTOR "
             "place the replicas and treat every cluster node as a candidate.")
    parser.add_argument(
        "--vip", metavar="ADDR/PREFIX", default="",
        help="virtual IP for the controller, e.g. 10.0.0.100/24. drbd-reactor "
             "brings it up on whichever node runs the controller (OCF agent "
             "ocf:heartbeat:IPaddr2), so clients and integrations can use one "
             "address instead of a list. Default: no VIP — clients get the "
             "list of controller candidates.")
    parser.add_argument(
        "--size", metavar="SIZE", default=DEFAULT_DB_SIZE,
        help="size of the controller database volume (default: %(default)s)")
    parser.add_argument(
        "-y", "--yes", action="store_true",
        help="do not ask for confirmation before changing anything")
    return parser.parse_args(argv)


def parse_vip(value):
    """--vip as (address, prefix), or None. Validated here rather than by
    drbd-reactor, which would only complain once the promoter runs."""
    if not value:
        return None
    if "/" not in value:
        die("--vip needs a prefix length, e.g. " + value + "/24")
    address, _, prefix = value.partition("/")
    try:
        socket.inet_aton(address)
    except OSError:
        die("--vip: '" + address + "' is not an IPv4 address")
    if not prefix.isdigit() or not 0 < int(prefix) <= 32:
        die("--vip: '" + prefix + "' is not a valid prefix length (1-32)")
    return address, int(prefix)


def split_nodes(value):
    """--nodes as an ordered, de-duplicated list."""
    names = []
    for name in value.split(","):
        name = name.strip()
        if name and name not in names:
            names.append(name)
    return names


def _run(cmd, check=False, capture=False):
    """subprocess.run for Python 3.6 and up.

    RHEL 8 ships Python 3.6, where `capture_output` and `text` do not exist
    yet (both are 3.7). Their long-hand equivalents work everywhere, so this
    script sticks to those rather than requiring a newer interpreter on the
    node it is meant to fix.
    """
    pipe = subprocess.PIPE if capture else None
    return subprocess.run(cmd, check=check, universal_newlines=True,
                          stdout=pipe, stderr=pipe)


def run(cmd, check=True, capture=False):
    print("+ " + " ".join(cmd), flush=True)
    return _run(cmd, check=check, capture=capture)


def out(cmd):
    return _run(cmd, check=True, capture=True).stdout


CONTROLLER_URI = "linstor://localhost"

_linstor_conn = []


def linstor_api():
    """A connected python-linstor client for the controller on this node.

    The library is imported here and not at the top: preflight() may still have
    to install linstor-client (which brings python-linstor with it), so at
    import time it is not necessarily there yet.

    The controller is always local -- the script only runs where it is active --
    and only until move_db() stops it, which is why every call has to happen
    before that.
    """
    if not _linstor_conn:
        # preflight() may have installed linstor-client moments ago. Python
        # caches the directory listing of every sys.path entry, so a package
        # that appeared after this interpreter started is not necessarily
        # visible to import without this.
        importlib.invalidate_caches()
        try:
            import linstor
        except ImportError:
            die("the python-linstor library is not importable by "
                + sys.executable + " — it ships with linstor-client")
        conn = linstor.Linstor(CONTROLLER_URI)
        try:
            conn.connect()
        except linstor.LinstorNetworkError as err:
            die("cannot reach the LINSTOR controller on this node: "
                + str(err))
        _linstor_conn.append(conn)
    return _linstor_conn[0]


def api_call(what, call, *call_args, **call_kwargs):
    """Announce a LINSTOR API call, make it, and fail on any error it reports.

    The steps this replaced were `linstor ...` command lines that run() echoed
    before running them, so announce these the same way and before the call:
    spawning a resource takes a while, and a run that appears to do nothing
    meanwhile is not one an operator can follow.
    """
    print("+ linstor: " + what, flush=True)
    responses = call(*call_args, **call_kwargs)
    for response in responses:
        if response.is_error():
            die(what + " failed: " + response.message)
    return responses


def die(msg):
    print("ERROR: " + msg, file=sys.stderr)
    sys.exit(1)


def local_node(cluster_nodes=None):
    """This node's name as LINSTOR knows it.

    Node names are whatever was registered: short on some clusters, fully
    qualified on others. Match the local hostname against the cluster's own
    list rather than assuming one form -- stripping the domain unconditionally
    makes this node look unregistered on an FQDN cluster.
    """
    host = socket.gethostname()
    short = host.split(".")[0]
    if cluster_nodes:
        for candidate in (host, short):
            if candidate in cluster_nodes:
                return candidate
        # same host registered under another spelling
        for node in cluster_nodes:
            if node.split(".")[0] == short:
                return node
    return short


def ask_yes(prompt):
    """y/N prompt; without a terminal to ask, the answer is no.

    The isatty check is not cosmetic: over an ssh session with no tty, stdin
    stays open without ever delivering a line, so input() blocks forever
    instead of raising EOFError. A script that hangs is worse than one that
    declines -- use --yes to answer up front.
    """
    if not sys.stdin.isatty():
        print(prompt + " [y/N] no (not a terminal; use --yes)")
        return False
    try:
        return input(prompt + " [y/N] ").strip().lower() == "y"
    except EOFError:
        return False


def confirm(prompt, assume_yes=False):
    if assume_yes:
        print(prompt + " yes (--yes)")
        return
    if not ask_yes(prompt):
        die("aborted by user")


def pkg_manager():
    """The node's package manager, as (name, install-cmd, refresh-cmd)."""
    for name, install, refresh in (
            ("apt-get", ["apt-get", "install", "-y"], ["apt-get", "update", "-q"]),
            ("dnf", ["dnf", "install", "-y"], None),
            ("yum", ["yum", "install", "-y"], None),
            ("zypper", ["zypper", "--non-interactive", "install"], None)):
        if shutil.which(name):
            return name, install, refresh
    return None, None, None


_pkg_lists_refreshed = []


def refresh_pkg_lists():
    """Bring the package lists up to date, at most once per run.

    Both the probe in pkg_known() and the install below need this. On a freshly
    provisioned node apt has no lists yet, so `apt-cache show` reports "no such
    package" for something the repository does carry -- and the probe then
    settles on the wrong package name.
    """
    if _pkg_lists_refreshed:
        return
    _pkg_lists_refreshed.append(True)
    _, _, refresh = pkg_manager()
    if refresh:
        run(refresh, check=False)


def install_packages(packages, missing_desc, assume_yes=False):
    """Offer to install missing packages from the repositories ALREADY
    configured on this node (typically the LINBIT customer repo or the
    drbd-9 PPA). This never adds a repository — picking the right source is
    deployment-specific, and drbd-dkms builds a kernel module.

    --yes covers this too: a run that promised not to ask anything cannot then
    stop at a prompt, which is also what makes the script usable unattended.
    """
    hint = ("install them from your LINBIT repository (or "
            "ppa:linbit/linbit-drbd9-stack on Ubuntu) and re-run this script")
    name, install, _ = pkg_manager()
    print("missing on this node: " + missing_desc)
    if name is None:
        die("no supported package manager found (apt-get/dnf/yum/zypper) — "
            + hint)
    print("Packages to install with " + name + ": " + " ".join(packages))
    print("They are taken from the repositories already configured here; "
          "no repository is added.")
    if assume_yes:
        print("Install them now? yes (--yes)")
    elif not ask_yes("Install them now?"):
        die("missing packages: " + " ".join(packages) + " — " + hint)
    refresh_pkg_lists()
    if run(install + list(packages), check=False).returncode != 0:
        die("installing " + " ".join(packages) + " failed — " + hint)


def unit_exists(unit):
    return _run(["systemctl", "cat", unit + ".service"],
                capture=True).returncode == 0


def write_file(path, content):
    print("+ write " + path, flush=True)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as fh:
        fh.write(content)


def drbd_kernel_version():
    """(major, minor) of the loaded DRBD kernel module, or None when no module
    is loaded. /proc/drbd only exists once the module is in."""
    try:
        with open("/proc/drbd") as fh:
            # "version: 9.3.2 (api:2/proto:118-124)"
            parts = fh.readline().split()
        major, minor = parts[1].split(".")[:2]
        return int(major), int(minor)
    except (OSError, IndexError, ValueError):
        return None


def check_drbd_module():
    """LINSTOR needs DRBD 9. Distributions ship an in-tree DRBD 8.4 module that
    can be loaded ahead of the 9.x one from drbd-dkms, in which case everything
    only breaks later with a confusing error."""
    version = drbd_kernel_version()
    if version is None:
        die("the DRBD kernel module is not loaded — install drbd-dkms (or "
            "drbd-module-$(uname -r)) and run 'modprobe drbd'")
    if version[0] < 9:
        die("DRBD kernel module " + str(version[0]) + "." + str(version[1])
            + " is loaded, but LINSTOR needs DRBD 9 — the distribution's "
            "in-tree module is in the way: install drbd-dkms, then "
            "'rmmod drbd && modprobe drbd'")


def check_reactor_can_read_drbd():
    """drbd-reactor drives the promoter off 'drbdsetup status --json'. When
    drbd-utils is too old for the installed reactor, that output cannot be
    deserialized and reactor just logs 'IGNORING resource ...' — the promoter
    then never fails over, with nothing obviously broken. Warn (not fail): the
    exact version pairing is not something this script can rule on."""
    try:
        resources = json.loads(out(["drbdsetup", "status", "--json"]))
    except Exception:
        print("WARNING: 'drbdsetup status --json' did not return usable JSON. "
              "drbd-reactor needs it to track the resource; check that "
              "drbd-utils and drbd-reactor versions fit together.")
        return
    needed = {"name", "role", "devices", "connections"}
    for res in resources:
        if not needed.issubset(res.keys()):
            print("WARNING: 'drbdsetup status --json' is missing fields "
                  + ", ".join(sorted(needed - set(res.keys())))
                  + " that drbd-reactor relies on — check that drbd-utils and "
                  "drbd-reactor versions fit together.")
            return


IPADDR2_AGENT = "/usr/lib/ocf/resource.d/heartbeat/IPaddr2"
# The OCF agents moved around: Ubuntu 22.04+ and Debian 12+ split them into
# resource-agents-base, older Ubuntu (20.04) and the RPM distributions keep the
# single resource-agents package.
IPADDR2_PACKAGES = ("resource-agents-base", "resource-agents")


def pkg_known(package):
    """Whether this node's package manager has ever heard of `package`."""
    refresh_pkg_lists()
    name, _, _ = pkg_manager()
    if name == "apt-get":
        query = ["apt-cache", "show", package]
    elif name in ("dnf", "yum"):
        query = [name, "info", "--quiet", package]
    elif name == "zypper":
        query = ["zypper", "--non-interactive", "info", package]
    else:
        return False
    return _run(query, capture=True).returncode == 0


def ipaddr2_package():
    """The package that carries the IPaddr2 OCF agent here. Ask the package
    manager instead of guessing from the distribution family: guessing gets
    Ubuntu 20.04 wrong, where the -base split does not exist yet."""
    for package in IPADDR2_PACKAGES:
        if pkg_known(package):
            return package
    return IPADDR2_PACKAGES[-1]


def missing_requirements(vip=None):
    """[(what, package)] for everything this node still needs. The controller
    itself is not listed — the script only runs where it is already active."""
    needed = []
    # "linstor" stands in for the whole linstor-client package: the script
    # imports its python-linstor library, and writes a CLI config for the
    # operator to use afterwards.
    for tool, pkg in (("linstor", "linstor-client"),
                      ("drbdadm", "drbd-utils"),
                      ("drbd-reactor", "drbd-reactor"),
                      ("mkfs.ext4", "e2fsprogs")):
        if shutil.which(tool) is None:
            needed.append((tool, pkg))
    if not unit_exists("linstor-satellite"):
        needed.append(("linstor-satellite.service", "linstor-satellite"))
    # The promoter only needs the OCF agent when a VIP was asked for.
    if vip and not os.path.exists(IPADDR2_AGENT):
        needed.append(("ocf:heartbeat:IPaddr2", ipaddr2_package()))
    return needed


def preflight(vip=None, assume_yes=False):
    if os.geteuid() != 0:
        die("run this script as root")
    missing = missing_requirements(vip)
    if missing:
        install_packages(sorted({pkg for _, pkg in missing}),
                         ", ".join(what for what, _ in missing),
                         assume_yes)
        missing = missing_requirements(vip)
        if missing:
            die("still missing after installing: "
                + ", ".join(what for what, _ in missing))
    # Only now that drbd-utils is guaranteed present: is the module usable?
    check_drbd_module()
    check_reactor_can_read_drbd()
    state = subprocess.run(["systemctl", "is-active", "--quiet",
                            "linstor-controller"]).returncode
    if state != 0:
        die("linstor-controller is not active on this node — run the "
            "script on the node currently running the controller")
    # The controller must be reachable before we start moving its DB.
    resources = linstor_api().resource_list(filter_by_resources=[RESOURCE])
    if resources and resources[0].resources:
        die("resource '" + RESOURCE + "' already exists — this cluster "
            "looks already (partially) HA-configured; resolve manually")
    if os.path.exists(MOUNT_UNIT_PATH):
        die(MOUNT_UNIT_PATH + " already exists — resolve manually")


def choose(prompt, options):
    """Interactive numbered picker; falls back to a clear error when there is
    no TTY (e.g. the script is being piped) so automation can pass the
    --storage-pool option instead."""
    for i, opt in enumerate(options, 1):
        print("  " + str(i) + ") " + opt)
    try:
        reply = input(prompt + " [1-" + str(len(options)) + "] ").strip()
    except EOFError:
        die("multiple storage pools and no TTY to choose — pass "
            "--storage-pool NAME")
    if reply.isdigit() and 1 <= int(reply) <= len(options):
        return options[int(reply) - 1]
    die("invalid selection: " + reply)


def resolve_storage_pool(requested):
    """Discover the storage pool from the live cluster. A single (non-diskless)
    pool is used automatically; with several the user picks one interactively.
    --storage-pool overrides the discovery (and skips the prompt)."""
    import linstor
    response = linstor_api().storage_pool_list()
    pools = sorted(
        {sp.name for sp in (response[0].storage_pools if response else [])
         if sp.provider_kind != linstor.StoragePoolDriver.Diskless})
    if requested:
        if requested not in pools:
            die("storage pool '" + requested + "' not found on this "
                "cluster — available: " + (", ".join(pools) or "none"))
        return requested
    if not pools:
        die("no storage pool found on this cluster — create one first")
    if len(pools) == 1:
        return pools[0]
    print("Multiple storage pools found; pick the one for the controller DB:")
    return choose("storage pool", pools)


def resolve_candidates(requested, cluster_nodes):
    """(controller candidates, pinned?). Without --nodes every cluster node is
    a candidate and LINSTOR decides where the replicas go."""
    me = local_node(cluster_nodes)
    if me not in cluster_nodes:
        # Otherwise the DB resource can never be brought up locally and the
        # migration would strand halfway.
        die("this node ('" + me + "') is not registered in the LINSTOR "
            "cluster — install/start linstor-satellite here and register "
            "it (nodes: " + (", ".join(cluster_nodes) or "none") + ")")
    if not requested:
        return cluster_nodes, False
    unknown = [n for n in requested if n not in cluster_nodes]
    if unknown:
        die("--nodes: not a cluster node: " + ", ".join(unknown)
            + " — cluster nodes: " + (", ".join(cluster_nodes) or "none"))
    if len(requested) < 2:
        die("--nodes needs at least 2 controller candidates, got "
            + str(len(requested)))
    if me not in requested:
        die("this node ('" + me + "') is not in --nodes — the controller "
            "database is moved here, so run the script on one of the "
            "selected nodes or add this one to the list")
    return requested, True


def cluster_facts():
    """{node name: first IP} of the live cluster, in cluster order. MUST be
    called while the controller is still running — everything after move_db()
    has to work without it."""
    response = linstor_api().node_list()
    facts = {}
    for node in (response[0].nodes if response else []):
        facts[node.name] = next(
            (nif.address for nif in node.net_interfaces if nif.address), "")
    return facts


def db_place_count(candidates, pinned, cluster_nodes):
    """Replica count for the controller DB: exactly the pinned candidates, or
    3 replicas (2 on a two-node cluster) chosen by LINSTOR."""
    if len(cluster_nodes) < 2:
        die("controller HA needs at least 2 nodes; this cluster has "
            + str(len(cluster_nodes)))
    count = len(candidates) if pinned else min(3, len(cluster_nodes))
    if count == 2:
        print("WARNING: only 2 replicas of the controller database — losing "
              "one node loses DRBD quorum; consider a third (diskless) node")
    return count


def rg_exists():
    groups = linstor_api().resource_group_list_raise()
    return any(g.name == RESOURCE_GROUP for g in groups.resource_groups)


# What the user guide asks of the controller's own resource: no auto-promote
# (the promoter decides who is Primary) and freeze rather than lie about the
# database when the node loses quorum or its disk.
DB_DRBD_OPTIONS = {
    "DrbdOptions/Resource/auto-promote": "no",
    "DrbdOptions/Resource/quorum": "majority",
    "DrbdOptions/Resource/on-suspended-primary-outdated": "force-secondary",
    "DrbdOptions/Resource/on-no-quorum": "io-error",
    "DrbdOptions/Resource/on-no-data-accessible": "io-error",
}


def create_db_resource(pool, candidates, pinned, place_count, size):
    import linstor
    api = linstor_api()
    if rg_exists():
        print("resource group " + RESOURCE_GROUP + " already exists, reusing")
    else:
        api_call(
            "create resource group " + RESOURCE_GROUP + " on " + pool
            + ", " + str(place_count) + " replicas",
            api.resource_group_create, RESOURCE_GROUP, storage_pool=[pool],
            place_count=place_count)
        api_call("create the volume group of " + RESOURCE_GROUP,
                 api.volume_group_create, RESOURCE_GROUP)
    api_call(
        "set the DRBD options of " + RESOURCE_GROUP,
        api.resource_group_modify, RESOURCE_GROUP,
        property_dict=DB_DRBD_OPTIONS)
    try:
        # Pinned placement gets the definitions from the group only; the
        # replicas are then placed on exactly the chosen nodes instead of
        # wherever the auto-placer would put them.
        api_call(
            "spawn " + RESOURCE + " (" + size + ") from " + RESOURCE_GROUP
            + (", definitions only" if pinned else ""),
            api.resource_group_spawn, RESOURCE_GROUP, RESOURCE, [size],
            definitions_only=pinned)
    except linstor.LinstorArgumentError as err:
        die("invalid --size '" + size + "': " + str(err))
    if not pinned:
        return
    api_call(
        "place " + RESOURCE + " on " + ", ".join(candidates),
        api.resource_create, [linstor.ResourceData(node, RESOURCE,
                                                   storage_pool=pool)
                              for node in candidates])


def wait_for_device(path, timeout=60):
    for _ in range(timeout):
        if os.path.exists(path):
            return
        time.sleep(1)
    die(path + " did not appear within " + str(timeout) + "s")


def move_db():
    wait_for_device("/dev/drbd/by-res/linstor_db/0")
    run(["systemctl", "disable", "--now", "linstor-controller"])
    write_file(MOUNT_UNIT_PATH, MOUNT_UNIT)
    run(["systemctl", "daemon-reload"])
    if os.path.exists("/var/lib/linstor.orig"):
        die("/var/lib/linstor.orig already exists — resolve manually")
    os.rename("/var/lib/linstor", "/var/lib/linstor.orig")
    os.mkdir("/var/lib/linstor")
    run(["drbdadm", "primary", RESOURCE])
    run(["mkfs.ext4", "-b", "4096", "/dev/drbd/by-res/linstor_db/0"])
    run(["systemctl", "start", "var-lib-linstor.mount"])
    run(["cp", "-a", "/var/lib/linstor.orig/.", "/var/lib/linstor/"])


def client_conf_text(ips, vip=None):
    """linstor-client config. With a VIP that single address always points at
    the active controller; without one the client is given every candidate so
    it can find whichever is up."""
    addresses = [vip[0]] if vip else (ips or ["localhost"])
    return "[global]\ncontrollers=" + ",".join(addresses) + "\n"


def controller_active():
    return subprocess.run(["systemctl", "is-active", "--quiet",
                           "linstor-controller"]).returncode == 0


def wait_controller(timeout_steps=30):
    for _ in range(timeout_steps):
        if controller_active():
            return True
        time.sleep(2)
    return False


def hand_off_to_reactor(controller_ips, vip=None):
    write_file(CLIENT_CONF_PATH, client_conf_text(controller_ips, vip))
    # The promoter snippet goes in before drbd-reactor is started, so it comes
    # up already knowing about the resource. Starting it first and reloading
    # afterwards is what makes a reload-on-change path unit necessary.
    write_file(REACTOR_CONF_PATH, reactor_conf(vip))
    run(["systemctl", "daemon-reload"])
    # Not `enable --now`: installing drbd-reactor starts it on the deb
    # distributions, and `--now` does nothing to a unit that is already active
    # -- the promoter snippet written just above would never be read.
    #
    # reload-or-restart rather than restart, because this node may already be
    # running drbd-reactor for other resources: the unit has
    # ExecReload=kill -HUP, so a reload picks up the new snippet without
    # dropping supervision of the promoters already there. Where the daemon is
    # not running yet, this starts it.
    run(["systemctl", "enable", "drbd-reactor"])
    run(["systemctl", "reload-or-restart", "drbd-reactor"])
    if not wait_controller():
        die("linstor-controller did not come back under drbd-reactor — "
            "check 'drbd-reactorctl status " + RESOURCE + "' and "
            "'journalctl -u drbd-reactor'")
    run(["drbd-reactorctl", "status", RESOURCE], check=False)


STANDBY_SCRIPT_PATH = "/tmp/linstor-controller-ha-standby.sh"


def standby_script_text(node_names, candidates, controller_ips, vip=None):
    """The setup for the other nodes, as one self-contained shell script.

    A file instead of printed instructions: it does not drown the run's real
    output, and it can be copied over and executed as-is — heredocs rarely
    survive a terminal copy-paste through bracketed-paste and auto-indent
    intact. Whoever prefers pasting can still copy from the file.
    """
    me = local_node(node_names)
    standby = [n for n in candidates if n != me] or ["<standby-node>"]
    rest = [n for n in node_names if n not in candidates]
    # Every candidate has to be able to bring the VIP up, so the OCF agent
    # belongs in their package list too.
    vip_pkg = ("""
[ -f """ + IPADDR2_AGENT + """ ] || missing="$missing """
               + ipaddr2_package() + '"') if vip else ""
    text = ("""#!/bin/sh
# Finish the LINSTOR controller HA setup on the nodes that did not run
# linstor-controller-ha-setup.py (generated by that script on '""" + me + """').
#
# Run as root on every standby controller candidate:
#     """ + ", ".join(standby) + """
""")
    if rest:
        # The nodes that never run a controller still need the client config:
        # without it the CLI on them keeps trying localhost, which is exactly
        # what this migration took away.
        text += ("""#
# On the remaining satellite nodes — the ones NOT becoming a controller
# candidate (""" + ", ".join(rest) + """) — run it with --client-only,
# which stops after pointing the linstor CLI at the candidates.
""")
    text += ("""
set -eu

[ "$(id -u)" = 0 ] || { echo "ERROR: run this script as root" >&2; exit 1; }

client_only=false
if [ "${1-}" = "--client-only" ]; then
  client_only=true
elif [ $# -gt 0 ]; then
  echo "usage: $0 [--client-only]" >&2
  exit 1
fi

mkdir -p /etc/linstor
cat <<'EOF' > """ + CLIENT_CONF_PATH + """
""" + client_conf_text(controller_ips, vip) + """EOF

if $client_only; then
  exit 0
fi

# --- required packages -------------------------------------------------
# Installed from the repositories already configured on this node (typically
# your LINBIT repository); no repository is added here.
missing=
command -v drbd-reactor >/dev/null || missing="$missing drbd-reactor"
systemctl cat linstor-controller.service >/dev/null 2>&1 || missing="$missing linstor-controller"
systemctl cat linstor-satellite.service >/dev/null 2>&1 || missing="$missing linstor-satellite\"""" + vip_pkg + """
if [ -n "$missing" ]; then
  echo "Installing missing packages:$missing"
  if command -v apt-get >/dev/null; then apt-get update -q && apt-get install -y $missing
  elif command -v dnf >/dev/null; then dnf install -y $missing
  elif command -v yum >/dev/null; then yum install -y $missing
  elif command -v zypper >/dev/null; then zypper --non-interactive install $missing
  else echo "ERROR: no supported package manager — install$missing manually"; exit 1
  fi
fi

systemctl disable --now linstor-controller

cat <<'EOF' > /etc/systemd/system/var-lib-linstor.mount
""" + MOUNT_UNIT + """EOF

mkdir -p /var/lib/linstor

cat <<'EOF' > """ + REACTOR_CONF_PATH + """
""" + reactor_conf(vip) + """EOF

systemctl daemon-reload
# Not `enable --now`: on the deb distributions drbd-reactor is already running
# after its install, and `--now` would leave it that way -- without ever reading
# the promoter snippet written above. reload-or-restart keeps any promoters this
# node already runs supervised, and starts the daemon where it is not running.
systemctl enable drbd-reactor
systemctl reload-or-restart drbd-reactor
# Informational — on a standby the resource simply stays Secondary.
drbd-reactorctl status """ + RESOURCE + """ || true
""")
    return text


def write_standby_script(content):
    print("+ write " + STANDBY_SCRIPT_PATH, flush=True)
    try:
        os.unlink(STANDBY_SCRIPT_PATH)
    except FileNotFoundError:
        pass
    # O_EXCL, not open(): /tmp is world-writable, so a plain open of this
    # well-known name would happily follow a symlink somebody planted there.
    fd = os.open(STANDBY_SCRIPT_PATH,
                 os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o700)
    with os.fdopen(fd, "w") as fh:
        fh.write(content)


def print_standby_instructions(facts, candidates, controller_ips, vip=None):
    node_names = list(facts)
    me = local_node(node_names)
    standby = [n for n in candidates if n != me] or ["<standby-node>"]
    rest = [n for n in node_names if n not in candidates]
    write_standby_script(
        standby_script_text(node_names, candidates, controller_ips, vip))

    def commands(node, extra=""):
        # ssh/scp by the address LINSTOR has registered, not the node name:
        # the name is whatever the node was registered as and does not have
        # to be resolvable from here.
        addr = facts.get(node) or node
        print("    scp " + STANDBY_SCRIPT_PATH + " " + addr + ":/tmp/")
        print("    ssh " + addr + " sh " + STANDBY_SCRIPT_PATH + extra
              + "   # " + node)

    print()
    print("=" * 72)
    print("DONE on this node. To finish the HA setup, run the generated")
    print("script as root on each standby controller node:")
    for node in standby:
        print()
        commands(node)
    if rest:
        # The satellite-only nodes never run a controller, but the linstor CLI
        # on them still has to find one — --client-only writes just that
        # config and stops.
        print()
        print("and with --client-only on every remaining satellite node,")
        print("which only points the linstor CLI at the candidates:")
        for node in rest:
            print()
            commands(node, " --client-only")
    print("=" * 72)
    print()
    print("Cluster nodes: " + ", ".join(node_names or ["<none>"]))
    print("Controller candidates: " + ", ".join(candidates or ["<none>"]))
    if vip:
        print("Controller VIP: " + vip[0] + "/" + str(vip[1])
              + " — point clients and integrations at this address.")
    print()
    if not vip:
        print("Integrations (Proxmox, CSI, ...) should also be pointed at the")
        print("list of all controller IPs.")
    print("Once you have verified failover, /var/lib/linstor.orig on this")
    print("node can be removed.")


def recovery_hint(vip=None):
    return """
The controller DB was already moved when this failed, so LINSTOR is down
until the promoter takes over. To finish by hand:

    cat <<'EOF' > """ + REACTOR_CONF_PATH + """
""" + reactor_conf(vip) + """EOF
    systemctl reload drbd-reactor || systemctl restart drbd-reactor
    drbd-reactorctl status """ + RESOURCE + """

To roll back instead (undo the move and run the controller as before):

    systemctl stop linstor-controller
    umount /var/lib/linstor && rmdir /var/lib/linstor
    mv /var/lib/linstor.orig /var/lib/linstor
    drbdadm secondary """ + RESOURCE + """
    rm -f """ + MOUNT_UNIT_PATH + " " + REACTOR_CONF_PATH + """
    systemctl daemon-reload && systemctl enable --now linstor-controller
"""


def main():
    args = parse_args()
    vip = parse_vip(args.vip)
    preflight(vip, args.yes)
    # Read everything the later steps need while the controller is still up:
    # once move_db() stops it, the linstor CLI cannot be used any more.
    facts = cluster_facts()
    node_names = list(facts)
    candidates, pinned = resolve_candidates(split_nodes(args.nodes),
                                            node_names)
    controller_ips = [facts[n] for n in candidates if facts.get(n)]
    pool = resolve_storage_pool(args.storage_pool)
    place_count = db_place_count(candidates, pinned, node_names)
    print("This will move the LINSTOR controller database onto a DRBD")
    print("resource ('" + RESOURCE + "', pool '" + pool + "', "
          + str(place_count) + " replicas) and hand the controller "
          "over to drbd-reactor.")
    print("Controller candidates: " + ", ".join(candidates)
          + ("" if pinned else " (all cluster nodes)"))
    if vip:
        print("Controller VIP: " + vip[0] + "/" + str(vip[1])
              + " (drbd-reactor brings it up on the active node)")
    confirm("Continue?", args.yes)
    create_db_resource(pool, candidates, pinned, place_count, args.size)
    move_db()
    # From here the controller is stopped: report how to recover if anything
    # goes wrong instead of leaving the node silently half-migrated.
    try:
        hand_off_to_reactor(controller_ips, vip)
    except Exception as err:
        print("ERROR: handing over to drbd-reactor failed: " + str(err),
              file=sys.stderr)
        print(recovery_hint(vip), file=sys.stderr)
        sys.exit(1)
    print_standby_instructions(facts, candidates, controller_ips, vip)


if __name__ == "__main__":
    main()
