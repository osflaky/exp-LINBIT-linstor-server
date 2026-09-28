package com.linbit.linstor.layer.dmsetup;

import com.linbit.extproc.ExtCmdFactory;
import com.linbit.extproc.utils.TestExtCmd;
import com.linbit.extproc.utils.TestExtCmd.Command;
import com.linbit.extproc.utils.TestExtCmd.TestOutputData;
import com.linbit.linstor.storage.StorageException;
import com.linbit.linstor.testutils.EmptyErrorReporter;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DmSetupUtilsTest
{
    private TestExtCmd extCmd;
    private ExtCmdFactory extCmdFactory;

    @Before
    public void setUp()
    {
        extCmd = new TestExtCmd(new EmptyErrorReporter());
        extCmdFactory = Mockito.mock(ExtCmdFactory.class);
        Mockito.when(extCmdFactory.create()).thenReturn(extCmd);
    }

    @After
    public void tearDown()
    {
        HashSet<Command> uncalledCommands = extCmd.getUncalledCommands();
        assertThat(uncalledCommands).isEmpty();
    }

    private void expect(int exitCode, String stdOut, String stdErr, String... cmd)
    {
        extCmd.setExpectedBehavior(new Command(cmd), new TestOutputData(cmd, stdOut, stdErr, exitCode));
    }

    @Test
    public void listParsesDeviceNames() throws StorageException
    {
        expect(
            0,
            "linstor_vg-rsc0_00000\t(253:4)\n" +
                "linstor_vg-thinpool_tmeta\t(253:1)\n" +
                "vg--with--dashes-lv\t(253, 12)\n",
            "",
            "dmsetup", "ls"
        );

        Set<String> devices = DmSetupUtils.list(extCmd, null);

        assertThat(devices).containsExactlyInAnyOrder(
            "linstor_vg-rsc0_00000",
            "linstor_vg-thinpool_tmeta",
            "vg--with--dashes-lv"
        );
    }

    @Test
    public void listWithTargetAddsTargetArgument() throws StorageException
    {
        expect(
            0,
            "rsc0_00000-writecache\t(253:7)\n",
            "",
            "dmsetup", "ls", "--target", "writecache"
        );

        Set<String> devices = DmSetupUtils.list(extCmd, "writecache");

        assertThat(devices).containsExactly("rsc0_00000-writecache");
    }

    @Test
    public void listWithoutDevicesReturnsEmptySet() throws StorageException
    {
        // dmsetup prints this info line when no device-mapper devices exist
        expect(0, "No devices found\n", "", "dmsetup", "ls");

        assertThat(DmSetupUtils.list(extCmd, null)).isEmpty();
    }

    @Test
    public void listFailureThrowsStorageException()
    {
        expect(1, "", "dmsetup: something went wrong\n", "dmsetup", "ls");

        assertThatThrownBy(() -> DmSetupUtils.list(extCmd, null))
            .isInstanceOf(StorageException.class);
    }

    @Test
    public void dirtyCacheBlocksAreParsedFromCacheStatus() throws StorageException
    {
        // dmsetup status output of a dm-cache device:
        // start len cache metadata_block_size used/total_meta cache_block_size used/total_cache
        // read_hits read_misses write_hits write_misses demotions promotions dirty ...
        expect(
            0,
            "0 409600 cache 8 27/2048 128 14/3200 348 55 28 24 0 14 42 1 writeback 2 " +
                "migration_threshold 2048 smq 0 rw -\n",
            "",
            "dmsetup", "status", "linstor_vg-rsc0_00000"
        );

        long dirtyBlocks = DmSetupUtils.getDirtyCacheBlocks(extCmdFactory, "linstor_vg-rsc0_00000");

        assertThat(dirtyBlocks).isEqualTo(42L);
    }

    @Test
    public void dirtyCacheBlocksOfNonCacheDeviceThrows()
    {
        // a writecache status line must be rejected by the safety check
        expect(
            0,
            "0 409600 writecache 0 3232 64 14\n",
            "",
            "dmsetup", "status", "rsc0_00000-writecache"
        );

        assertThatThrownBy(() -> DmSetupUtils.getDirtyCacheBlocks(extCmdFactory, "rsc0_00000-writecache"))
            .isInstanceOf(StorageException.class)
            .hasMessageContaining("Expected 'cache'")
            .hasMessageContaining("writecache");
    }

    @Test
    public void dirtyCacheBlocksFailingCommandThrows()
    {
        expect(1, "", "Device does not exist.\n", "dmsetup", "status", "gone");

        assertThatThrownBy(() -> DmSetupUtils.getDirtyCacheBlocks(extCmdFactory, "gone"))
            .isInstanceOf(StorageException.class);
    }

    @Test
    public void removeExecutesDmsetupRemoveWithRetry() throws StorageException
    {
        expect(0, "", "", "dmsetup", "remove", "--retry", "linstor_vg-rsc0_00000");

        DmSetupUtils.remove(extCmd, "linstor_vg-rsc0_00000");
    }

    @Test
    public void removeFailureThrowsStorageException()
    {
        expect(1, "", "device is busy\n", "dmsetup", "remove", "--retry", "busyDev");

        assertThatThrownBy(() -> DmSetupUtils.remove(extCmd, "busyDev"))
            .isInstanceOf(StorageException.class)
            .hasMessageContaining("Failed to remove writecache device");
    }
}
