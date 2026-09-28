package com.linbit.extproc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the OR-combined I/O progress decision used by
 * {@link ChildProcessHandler#waitForWithIoProgress()}: progress is signalled by either the
 * per-process {@code /proc/<pid>/io} counter (dirtying phase) or the device-global
 * {@code /sys/.../stat} counter (writeback/flush phase).
 */
@SuppressWarnings("checkstyle:magicnumber")
public class ChildProcessHandlerIoProgressTest
{
    private static final long NONE = -1;

    @Test
    public void pidCounterAdvancingIsProgress()
    {
        // classic dirtying phase: /proc/<pid>/io climbs, no devices monitored
        assertTrue(ChildProcessHandler.hasIoProgress(200, 100, NONE, NONE));
    }

    @Test
    public void pidCounterUnreadableIsProgress()
    {
        // process vanished between waitFor() and the /proc read -> optimistically assume progress
        assertTrue(ChildProcessHandler.hasIoProgress(NONE, 100, NONE, NONE));
        // even if the device counter is stalled
        assertTrue(ChildProcessHandler.hasIoProgress(NONE, 100, 500, 500));
    }

    @Test
    public void deviceCounterAdvancingWhilePidStalledIsProgress()
    {
        // the flush phase the whole feature is about: pid counter frozen, device still writing back
        assertTrue(ChildProcessHandler.hasIoProgress(200, 200, 800, 500));
    }

    @Test
    public void firstDeviceReadIsProgress()
    {
        // first poll with a monitored device: lastDeviceSectors is still the -1 seed
        assertTrue(ChildProcessHandler.hasIoProgress(200, 200, 500, NONE));
    }

    @Test
    public void bothCountersStalledIsNoProgress()
    {
        // neither the process nor any monitored device advanced -> genuine stall
        assertFalse(ChildProcessHandler.hasIoProgress(200, 200, 800, 800));
    }

    @Test
    public void pidStalledWithoutDevicesIsNoProgress()
    {
        // no devices configured (deviceSectors == -1) -> falls back to pid-only, which is stalled
        assertFalse(ChildProcessHandler.hasIoProgress(200, 200, NONE, NONE));
    }

    @Test
    public void deviceBecomingUnreadableDoesNotForceProgress()
    {
        // a monitored device that turns unreadable must not, by itself, be read as progress
        assertFalse(ChildProcessHandler.hasIoProgress(200, 200, NONE, 800));
    }
}
