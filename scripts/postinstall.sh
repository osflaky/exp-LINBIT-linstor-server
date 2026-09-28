#!/bin/sh

DEF_LOCATION="/usr/share/linstor-server"
# DEF_USER="linstor"
# DEF_PWD="linstor"
DEF_VARLIB_LINSTOR="/var/lib/linstor"
DEF_DB="${DEF_VARLIB_LINSTOR}/linstordb"
DEF_LINSTOR_CFG_DIR="/etc/linstor"
DEF_LINSTOR_CFG="${DEF_LINSTOR_CFG_DIR}/linstor.toml"
DEF_DB_TYPE="h2"

[ ! -d ${DEF_LINSTOR_CFG_DIR} ] && mkdir "$DEF_LINSTOR_CFG_DIR"
[ ! -d ${DEF_VARLIB_LINSTOR} ] && mkdir -m 750 "$DEF_VARLIB_LINSTOR"

# always create a backup of the current DB
CURRENT_DB=${DEF_DB}.mv.db
[ -f "$CURRENT_DB" ] && cp "$CURRENT_DB" "${CURRENT_DB}-$(date --iso-8601=minutes).bak"

[ ! -f ${DEF_LINSTOR_CFG} ] && ${DEF_LOCATION}/bin/linstor-config create-db-file --dbtype=${DEF_DB_TYPE} ${DEF_DB} > ${DEF_LINSTOR_CFG}

# migrate databases written by H2 1.x to the new H2 database format
h2_migrate() {
    # cheap prefilter to avoid a JVM launch for non-h2 (etcd/k8s/postgres/...) setups:
    # take the file path from an h2 connection_url in linstor.toml, fall back to the built-in
    # default when no connection_url is configured at all
    DB_PATH=$(sed -n 's/^[[:space:]]*connection_url[[:space:]]*=[[:space:]]*"jdbc:h2:\(file:\)\{0,1\}\([^;"]*\).*/\2/p' "$DEF_LINSTOR_CFG" 2>/dev/null | head -n1)
    if [ -z "$DB_PATH" ]; then
        grep -q '^[[:space:]]*connection_url' "$DEF_LINSTOR_CFG" 2>/dev/null && return 0
        DB_PATH="$DEF_DB"
    fi
    NEED_MIGRATE=0
    if [ -f "${DB_PATH}.mv.db" ]; then
        head -c 4096 "${DB_PATH}.mv.db" | grep -q ',format:1,' && NEED_MIGRATE=1
    elif [ -f "${DB_PATH}.h2.db" ]; then
        # legacy PageStore database
        NEED_MIGRATE=1
    fi
    [ "$NEED_MIGRATE" = 1 ] || return 0

    HAVE_SYSTEMD=0
    [ -d /run/systemd/system ] && HAVE_SYSTEMD=1

    # does an active drbd-reactor have a promoter plugin managing linstor-controller.service?
    reactor_manages_controller() {
        systemctl is-active --quiet drbd-reactor.service || return 1
        # if the plugin configuration cannot be inspected, err on the side of not touching anything
        command -v drbd-reactorctl > /dev/null 2>&1 || return 0
        drbd-reactorctl status 2>/dev/null | grep -q 'linstor-controller\.service'
    }

    if [ "$HAVE_SYSTEMD" = 1 ] && reactor_manages_controller; then
        echo "INFO: The LINSTOR database at ${DB_PATH} needs to be migrated to the new H2 format," >&2
        echo "INFO: but drbd-reactor manages the controller, so it will not be touched by the package upgrade." >&2
        echo "INFO: The controller migrates the database automatically on its next start; to migrate" >&2
        echo "INFO: manually, stop the controller and run: linstor-database migrate-h2" >&2
        return 0
    fi

    WAS_ACTIVE=0
    if [ "$HAVE_SYSTEMD" = 1 ] && systemctl is-active --quiet linstor-controller.service; then
        WAS_ACTIVE=1
        echo "Stopping linstor-controller for the H2 database migration"
        systemctl stop linstor-controller.service || true
    fi
    if ${DEF_LOCATION}/bin/linstor-database migrate-h2 --yes -c ${DEF_LINSTOR_CFG_DIR}; then
        [ "$WAS_ACTIVE" = 1 ] && systemctl start linstor-controller.service || true
    else
        echo "WARNING: The automatic H2 database migration FAILED, the original database was kept." >&2
        echo "WARNING: The controller retries the migration on its next start; if that also fails" >&2
        echo "WARNING: it will refuse to start until 'linstor-database migrate-h2' succeeds." >&2
    fi
}
h2_migrate

# DEBHELPER will be replaced by debian build system, adding systemd helper scripts and so on...
#DEBHELPER#

exit 0
