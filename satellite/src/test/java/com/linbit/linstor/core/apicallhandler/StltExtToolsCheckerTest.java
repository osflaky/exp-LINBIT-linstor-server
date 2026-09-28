package com.linbit.linstor.core.apicallhandler;

import com.linbit.Platform;
import com.linbit.drbd.DrbdVersion;
import com.linbit.extproc.ExtCmdFactory;
import com.linbit.extproc.utils.TestExtCmd;
import com.linbit.linstor.core.cfg.StltConfig;
import com.linbit.linstor.layer.drbd.drbdstate.DrbdEventService;
import com.linbit.linstor.layer.storage.spdk.utils.SpdkLocalCommands;
import com.linbit.linstor.layer.storage.utils.SysClassUtils;
import com.linbit.linstor.storage.kinds.ExtTools;
import com.linbit.linstor.storage.kinds.ExtToolsInfo;
import com.linbit.linstor.testutils.EmptyErrorReporter;

import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the version parsing of the various external tool checks of {@link StltExtToolsChecker} by
 * feeding canned command outputs through a {@link TestExtCmd}.
 *
 * Note: checks depending on the content of /proc/modules (DM_WRITECACHE, DM_CACHE and parts of NVME /
 * BCACHE_TOOLS) are made deterministic by registering "modprobe" calls that succeed (or fail), so the
 * outcome is the same whether or not the module is already loaded on the host running the tests.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class StltExtToolsCheckerTest
{
    private static final String[] CMD_DRBD_PROXY = {"drbd-proxy", "--version"};
    private static final String[] CMD_DRBD_REACTOR = {"drbd-reactorctl", "--version"};
    private static final String[] CMD_CRYPTSETUP = {"cryptsetup", "--version"};
    private static final String[] CMD_LVM = {"lvm", "version"};
    private static final String[] CMD_LVM_THIN = {"thin_check", "-V"};
    private static final String[] CMD_THIN_SEND = {"thin_send", "-v"};
    private static final String[] CMD_ZFS_KMOD = {"cat", "/sys/module/zfs/version"};
    private static final String[] CMD_ZFS_UTILS = {"zfs", "--version"};
    private static final String[] CMD_MODPROBE_NVMET_RDMA = {"modprobe", "nvmet_rdma"};
    private static final String[] CMD_MODPROBE_NVME_RDMA = {"modprobe", "nvme_rdma"};
    private static final String[] CMD_NVME = {"nvme", "version"};
    private static final String[] CMD_SPDK = {SpdkLocalCommands.SPDK_RPC_SCRIPT, "spdk_get_version"};
    private static final String[] CMD_EBS_INIT = {"cat", "/sys/devices/virtual/dmi/id/board_asset_tag"};
    private static final String[] CMD_MODPROBE_WRITECACHE = {"modprobe", "dm-writecache"};
    private static final String[] CMD_MODPROBE_DM_CACHE = {"modprobe", "dm-cache"};
    private static final String[] CMD_MODPROBE_BCACHE = {"modprobe", "bcache"};
    private static final String[] CMD_MAKE_BCACHE = {"make-bcache", "-h"};
    private static final String[] CMD_LOSETUP = {"losetup", "--version"};
    private static final String[] CMD_ZSTD = {"zstd", "-V"};
    private static final String[] CMD_SOCAT = {"socat", "-V"};
    private static final String[] CMD_TIMEOUT = {"timeout", "--version"};
    private static final String[] CMD_UDEVADM = {"udevadm", "version"};
    private static final String[] CMD_LSSCSI = {"lsscsi", "--version"};

    private static final String NOT_FOUND = "command not found";

    private TestExtCmd testExtCmd;
    private StltConfig stltCfgMock;
    private StltExtToolsChecker checker;

    @Before
    public void setUp()
    {
        // the checker array of StltExtToolsChecker is platform dependent, these tests cover the Linux checks
        Assume.assumeTrue(Platform.isLinux());

        testExtCmd = new TestExtCmd(new EmptyErrorReporter());
        ExtCmdFactory extCmdFactory = Mockito.mock(ExtCmdFactory.class);
        Mockito.when(extCmdFactory.create()).thenReturn(testExtCmd);

        DrbdVersion drbdVersion = new DrbdVersion(extCmdFactory, new EmptyErrorReporter());

        stltCfgMock = Mockito.mock(StltConfig.class);
        Mockito.when(stltCfgMock.isRemoteSpdk()).thenReturn(false);
        Mockito.when(stltCfgMock.isEbs()).thenReturn(false);

        DrbdEventService drbdEventServiceMock = Mockito.mock(DrbdEventService.class);
        Mockito.when(drbdEventServiceMock.isStarted()).thenReturn(true);

        checker = new StltExtToolsChecker(
            new EmptyErrorReporter(),
            drbdVersion,
            extCmdFactory,
            stltCfgMock,
            drbdEventServiceMock
        );
    }

    private void expect(int exitCode, String stdOut, String stdErr, String... cmd)
    {
        testExtCmd.setExpectedBehavior(
            new TestExtCmd.Command(cmd),
            new TestExtCmd.TestOutputData(cmd, stdOut, stdErr, exitCode)
        );
    }

    private void expectDrbdAdmVersion(int kMaj, int kMin, int kPatch, int uMaj, int uMin, int uPatch)
    {
        String stdOut = "DRBDADM_BUILDTAG=GIT-hash\n" +
            "DRBDADM_API_VERSION=2\n" +
            String.format("DRBD_KERNEL_VERSION_CODE=0x%02x%02x%02x%n", kMaj, kMin, kPatch) +
            String.format("DRBD_KERNEL_VERSION=%d.%d.%d%n", kMaj, kMin, kPatch) +
            String.format("DRBDADM_VERSION_CODE=0x%02x%02x%02x%n", uMaj, uMin, uPatch) +
            String.format("DRBDADM_VERSION=%d.%d.%d%n", uMaj, uMin, uPatch);
        expect(0, stdOut, "", DrbdVersion.VSN_QUERY_COMMAND);
    }

    /**
     * Registers realistic outputs for every command based check, simulating a host where all external
     * tools are installed.
     */
    private void registerAllToolsPresent()
    {
        expectDrbdAdmVersion(9, 1, 23, 9, 31, 0);
        expect(0, "Drbd-proxy 3.2.2\n", "", CMD_DRBD_PROXY);
        expect(0, "drbd-reactorctl 1.4.0\n", "", CMD_DRBD_REACTOR);
        expect(0, "cryptsetup 2.4.3 flags: UDEV BLKID KEYRING KERNEL_CAPI\n", "", CMD_CRYPTSETUP);
        expect(
            0,
            """
              LVM version:     2.03.11(2) (2021-01-08)
              Library version: 1.02.175 (2021-01-08)
              Driver version:  4.45.0
            """,
            "",
            CMD_LVM
        );
        expect(0, "0.9.0\n", "", CMD_LVM_THIN);
        expect(0, "1.0.4\n", "", CMD_THIN_SEND);
        expect(0, "2.1.5-1ubuntu6~22.04.5\n", "", CMD_ZFS_KMOD);
        expect(0, "zfs-2.1.5-1ubuntu6~22.04.5\nzfs-kmod-2.1.5-1ubuntu6~22.04.5\n", "", CMD_ZFS_UTILS);
        expect(0, "", "", CMD_MODPROBE_NVMET_RDMA);
        expect(0, "", "", CMD_MODPROBE_NVME_RDMA);
        expect(0, "nvme version 1.16\n", "", CMD_NVME);
        expect(0, "{\n  \"version\": \"SPDK v22.01.1 git sha1 abcdef123\"\n}\n", "", CMD_SPDK);
        expect(0, "i-0123456789abcdef0\n", "", CMD_EBS_INIT);
        expect(0, "", "", CMD_MODPROBE_WRITECACHE);
        expect(0, "", "", CMD_MODPROBE_DM_CACHE);
        expect(0, "", "", CMD_MODPROBE_BCACHE);
        expect(1, "Usage: make-bcache [options] device\n", "", CMD_MAKE_BCACHE);
        expect(0, "losetup from util-linux 2.37.4\n", "", CMD_LOSETUP);
        expect(0, "*** zstd command line interface 64-bits v1.5.2, by Yann Collet ***\n", "", CMD_ZSTD);
        expect(
            0,
            "socat by Gerhard Rieger and contributors\nsocat version 1.7.4.1 on Feb  3 2022 12:57:56\n",
            "",
            CMD_SOCAT
        );
        expect(0, "timeout (GNU coreutils) 8.32\nCopyright (C) 2020 Free Software Foundation\n", "", CMD_TIMEOUT);
        expect(0, "249\n", "", CMD_UDEVADM);
        // lsscsi prints its version information to stderr
        expect(0, "", "version: 0.31  2019/06/10 [svn: r153]\n", CMD_LSSCSI);
        expect(0, "0x5000c500a1b2c3d4\n", "", SysClassUtils.CMD_CAT_SAS_PHY);
        expect(0, "0x5000c500a1b2c3d5\n", "", SysClassUtils.CMD_CAT_SAS_DEVICE);
    }

    /**
     * Registers failing outputs for every command based check, simulating a host where no external
     * tools are installed at all.
     */
    private void registerAllToolsMissing()
    {
        expect(127, "", NOT_FOUND, DrbdVersion.VSN_QUERY_COMMAND);
        expect(127, "", NOT_FOUND, CMD_DRBD_PROXY);
        expect(127, "", NOT_FOUND, CMD_DRBD_REACTOR);
        expect(127, "", NOT_FOUND, CMD_CRYPTSETUP);
        expect(127, "", NOT_FOUND, CMD_LVM);
        expect(127, "", NOT_FOUND, CMD_LVM_THIN);
        expect(127, "", NOT_FOUND, CMD_THIN_SEND);
        expect(1, "", "cat: /sys/module/zfs/version: No such file or directory", CMD_ZFS_KMOD);
        expect(127, "", NOT_FOUND, CMD_ZFS_UTILS);
        expect(1, "", "modprobe: FATAL: Module nvmet_rdma not found", CMD_MODPROBE_NVMET_RDMA);
        expect(1, "", "modprobe: FATAL: Module nvme_rdma not found", CMD_MODPROBE_NVME_RDMA);
        expect(127, "", NOT_FOUND, CMD_NVME);
        expect(127, "", NOT_FOUND, CMD_SPDK);
        expect(1, "", "cat: No such file or directory", CMD_EBS_INIT);
        expect(1, "", "modprobe: FATAL: Module dm-writecache not found", CMD_MODPROBE_WRITECACHE);
        expect(1, "", "modprobe: FATAL: Module dm-cache not found", CMD_MODPROBE_DM_CACHE);
        expect(1, "", "modprobe: FATAL: Module bcache not found", CMD_MODPROBE_BCACHE);
        expect(127, "", NOT_FOUND, CMD_MAKE_BCACHE);
        expect(127, "", NOT_FOUND, CMD_LOSETUP);
        expect(127, "", NOT_FOUND, CMD_ZSTD);
        expect(127, "", NOT_FOUND, CMD_SOCAT);
        expect(127, "", NOT_FOUND, CMD_TIMEOUT);
        expect(127, "", NOT_FOUND, CMD_UDEVADM);
        expect(127, "", NOT_FOUND, CMD_LSSCSI);
        expect(1, "", "cat: '/sys/class/sas_phy/*/sas_address': No such file", SysClassUtils.CMD_CAT_SAS_PHY);
        expect(1, "", "cat: '/sys/class/sas_device/...': No such file", SysClassUtils.CMD_CAT_SAS_DEVICE);
    }

    private void assertVersion(
        Map<ExtTools, ExtToolsInfo> tools,
        ExtTools tool,
        Integer expectedMajor,
        Integer expectedMinor,
        Integer expectedPatch
    )
    {
        ExtToolsInfo info = tools.get(tool);
        assertThat(info.isSupported()).as("%s should be supported", tool).isTrue();
        assertThat(info.getVersionMajor()).as("%s major version", tool).isEqualTo(expectedMajor);
        assertThat(info.getVersionMinor()).as("%s minor version", tool).isEqualTo(expectedMinor);
        assertThat(info.getVersionPatch()).as("%s patch version", tool).isEqualTo(expectedPatch);
    }

    private void assertNotSupported(Map<ExtTools, ExtToolsInfo> tools, ExtTools tool)
    {
        assertThat(tools.get(tool).isSupported()).as("%s should NOT be supported", tool).isFalse();
    }

    @Test
    public void allToolsPresentParsesAllVersions()
    {
        registerAllToolsPresent();

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertVersion(tools, ExtTools.DRBD9_KERNEL, 9, 1, 23);
        assertVersion(tools, ExtTools.DRBD9_UTILS, 9, 31, 0);
        assertVersion(tools, ExtTools.DRBD_PROXY, 3, 2, 2);
        assertVersion(tools, ExtTools.DRBD_REACTOR, 1, 4, 0);
        assertVersion(tools, ExtTools.CRYPT_SETUP, 2, 4, 3);
        assertVersion(tools, ExtTools.LVM, 2, 3, 11);
        assertVersion(tools, ExtTools.LVM_THIN, 0, 9, 0);
        assertVersion(tools, ExtTools.THIN_SEND_RECV, 1, 0, null);
        assertVersion(tools, ExtTools.ZFS_KMOD, 2, 1, 5);
        assertVersion(tools, ExtTools.ZFS_UTILS, 2, 1, 5);
        assertThat(tools.get(ExtTools.ZFS_UTILS).getVersion().toString()).isEqualTo("2.1.5-1ubuntu6~22.04.5");
        assertVersion(tools, ExtTools.NVME, 1, 16, null);
        assertVersion(tools, ExtTools.SPDK, 22, 1, null);
        assertVersion(tools, ExtTools.LOSETUP, 2, 37, null);
        assertVersion(tools, ExtTools.ZSTD, 1, 5, null);
        assertVersion(tools, ExtTools.SOCAT, 1, 7, null);
        assertVersion(tools, ExtTools.COREUTILS_LINUX, 8, 32, null);
        assertVersion(tools, ExtTools.UDEVADM, 249, null, null);
        // lsscsi version is parsed from stderr
        assertVersion(tools, ExtTools.LSSCSI, 0, 31, null);

        // tools without version parsing
        assertThat(tools.get(ExtTools.EBS_INIT).isSupported()).isTrue();
        assertThat(tools.get(ExtTools.DM_WRITECACHE).isSupported()).isTrue();
        assertThat(tools.get(ExtTools.DM_CACHE).isSupported()).isTrue();
        assertThat(tools.get(ExtTools.BCACHE_TOOLS).isSupported()).isTrue();
        assertThat(tools.get(ExtTools.SAS_PHY).isSupported()).isTrue();
        assertThat(tools.get(ExtTools.SAS_DEVICE).isSupported()).isTrue();

        // not an EBS satellite
        assertNotSupported(tools, ExtTools.EBS_TARGET);
        // windows only tool
        assertNotSupported(tools, ExtTools.STORAGE_SPACES);
        assertThat(tools.get(ExtTools.STORAGE_SPACES).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("does not exist on the Linux platform"));
    }

    @Test
    public void allToolsMissingReportsNotSupported()
    {
        registerAllToolsMissing();

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertNotSupported(tools, ExtTools.DRBD9_KERNEL);
        assertNotSupported(tools, ExtTools.DRBD9_UTILS);
        assertNotSupported(tools, ExtTools.DRBD_PROXY);
        assertNotSupported(tools, ExtTools.DRBD_REACTOR);
        assertNotSupported(tools, ExtTools.CRYPT_SETUP);
        assertNotSupported(tools, ExtTools.LVM);
        assertNotSupported(tools, ExtTools.LVM_THIN);
        assertNotSupported(tools, ExtTools.THIN_SEND_RECV);
        assertNotSupported(tools, ExtTools.ZFS_KMOD);
        assertNotSupported(tools, ExtTools.ZFS_UTILS);
        assertNotSupported(tools, ExtTools.NVME);
        assertNotSupported(tools, ExtTools.SPDK);
        assertNotSupported(tools, ExtTools.EBS_INIT);
        assertNotSupported(tools, ExtTools.EBS_TARGET);
        assertNotSupported(tools, ExtTools.BCACHE_TOOLS);
        assertNotSupported(tools, ExtTools.LOSETUP);
        assertNotSupported(tools, ExtTools.ZSTD);
        assertNotSupported(tools, ExtTools.SOCAT);
        assertNotSupported(tools, ExtTools.COREUTILS_LINUX);
        assertNotSupported(tools, ExtTools.UDEVADM);
        assertNotSupported(tools, ExtTools.LSSCSI);
        assertNotSupported(tools, ExtTools.SAS_PHY);
        assertNotSupported(tools, ExtTools.SAS_DEVICE);
        assertNotSupported(tools, ExtTools.STORAGE_SPACES);
        // DM_WRITECACHE and DM_CACHE are deliberately not asserted here since their outcome depends
        // on whether the dm-writecache / dm-cache modules are already loaded on the test host

        assertThat(tools.get(ExtTools.LVM).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("exit code 127"));
        assertThat(tools.get(ExtTools.DRBD9_KERNEL).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("DRBD version has to be >= 9"));
    }

    @Test
    public void drbdKernelTooOldIsNotSupported()
    {
        registerAllToolsMissing();
        expectDrbdAdmVersion(8, 4, 11, 9, 31, 0);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertNotSupported(tools, ExtTools.DRBD9_KERNEL);
        assertThat(tools.get(ExtTools.DRBD9_KERNEL).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("DRBD version has to be >= 9"));
        // utils version is recent enough
        assertVersion(tools, ExtTools.DRBD9_UTILS, 9, 31, 0);
    }

    @Test
    public void drbdUtilsTooOldIsNotSupported()
    {
        registerAllToolsMissing();
        expectDrbdAdmVersion(9, 1, 23, 8, 9, 9);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertVersion(tools, ExtTools.DRBD9_KERNEL, 9, 1, 23);
        assertNotSupported(tools, ExtTools.DRBD9_UTILS);
        assertThat(tools.get(ExtTools.DRBD9_UTILS).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("DRBD utils version has to be >= 8.9.10"));
    }

    @Test
    public void cryptsetupUnparsableVersionOutput()
    {
        registerAllToolsMissing();
        expect(0, "something completely unexpected\n", "", CMD_CRYPTSETUP);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertNotSupported(tools, ExtTools.CRYPT_SETUP);
        assertThat(tools.get(ExtTools.CRYPT_SETUP).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("Failed to parse version"));
    }

    @Test
    public void lvmUnparsableVersionOutput()
    {
        registerAllToolsMissing();
        expect(0, "LVM version is unknown here\n", "", CMD_LVM);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertNotSupported(tools, ExtTools.LVM);
        assertThat(tools.get(ExtTools.LVM).getNotSupportedReasons())
            .anySatisfy(reason -> assertThat(reason).contains("Failed to parse version"));
    }

    @Test
    public void zfsUtilsWithoutVersionSubcommandIsSupportedWithUnknownVersion()
    {
        registerAllToolsMissing();
        // pre 0.8.0 zfs has no --version, but exits with 2 (unknown subcommand) and prints usage
        expect(2, "", "unrecognized command '--version'\nusage: zfs command args ...\n", CMD_ZFS_UTILS);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        ExtToolsInfo zfsInfo = tools.get(ExtTools.ZFS_UTILS);
        assertThat(zfsInfo.isSupported()).isTrue();
        assertThat(zfsInfo.getVersionMajor()).isNull();
        assertThat(zfsInfo.getVersionMinor()).isNull();
        assertThat(zfsInfo.getVersionPatch()).isNull();
    }

    @Test
    public void nvmeSupportedWhenRemoteSpdk()
    {
        registerAllToolsMissing();
        Mockito.when(stltCfgMock.isRemoteSpdk()).thenReturn(true);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        // remote SPDK satellites do not need local nvme tooling
        assertThat(tools.get(ExtTools.NVME).isSupported()).isTrue();
    }

    @Test
    public void ebsTargetSupportedOnEbsSatellite()
    {
        registerAllToolsMissing();
        Mockito.when(stltCfgMock.isEbs()).thenReturn(true);

        Map<ExtTools, ExtToolsInfo> tools = checker.getExternalTools(true);

        assertThat(tools.get(ExtTools.EBS_TARGET).isSupported()).isTrue();
    }

    @Test
    public void externalToolsAreCachedUntilRecache()
    {
        registerAllToolsMissing();

        Map<ExtTools, ExtToolsInfo> first = checker.getExternalTools(true);
        Map<ExtTools, ExtToolsInfo> second = checker.getExternalTools(false);
        assertThat(second).isSameAs(first);

        Map<ExtTools, ExtToolsInfo> third = checker.getExternalTools(true);
        assertThat(third).isNotSameAs(first);
    }

    @Test
    public void areSupportedReflectsToolSupport()
    {
        registerAllToolsPresent();

        assertThat(checker.areSupported(true, ExtTools.CRYPT_SETUP, ExtTools.LVM, ExtTools.ZFS_UTILS)).isTrue();
        assertThat(checker.areSupported(false, ExtTools.LVM, ExtTools.STORAGE_SPACES)).isFalse();
    }
}
