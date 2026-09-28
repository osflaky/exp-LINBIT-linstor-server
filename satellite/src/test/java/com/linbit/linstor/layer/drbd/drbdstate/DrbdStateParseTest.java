package com.linbit.linstor.layer.drbd.drbdstate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the label parsers of the drbdsetup events2 state enums.
 */
public class DrbdStateParseTest
{
    @Test
    public void parseRoleRoundTrip()
    {
        for (DrbdResource.Role role : DrbdResource.Role.values())
        {
            assertThat(DrbdResource.Role.parseRole(role.toString())).isEqualTo(role);
        }
    }

    @Test
    public void parseRoleUnknownLabel()
    {
        assertThat(DrbdResource.Role.parseRole("NoSuchRole")).isEqualTo(DrbdResource.Role.UNKNOWN);
        assertThat(DrbdResource.Role.parseRole("")).isEqualTo(DrbdResource.Role.UNKNOWN);
        // parsing is case sensitive
        assertThat(DrbdResource.Role.parseRole("primary")).isEqualTo(DrbdResource.Role.UNKNOWN);
    }

    @Test
    public void parseConnectionStateRoundTrip()
    {
        for (DrbdConnection.State state : DrbdConnection.State.values())
        {
            assertThat(DrbdConnection.State.parseState(state.toString())).isEqualTo(state);
        }
    }

    @Test
    public void parseConnectionStateUnknownLabel()
    {
        assertThat(DrbdConnection.State.parseState("NoSuchState")).isEqualTo(DrbdConnection.State.UNKNOWN);
        assertThat(DrbdConnection.State.parseState("connected")).isEqualTo(DrbdConnection.State.UNKNOWN);
    }

    @Test
    public void parseDiskStateRoundTrip()
    {
        for (DiskState diskState : DiskState.values())
        {
            assertThat(DiskState.parseDiskState(diskState.toString())).isEqualTo(diskState);
        }
    }

    @Test
    public void parseDiskStateUnknownLabel()
    {
        assertThat(DiskState.parseDiskState("NoSuchState")).isEqualTo(DiskState.UNKNOWN);
        // the label of the unknown disk state is "DUnknown", plain "Unknown" is not a valid label
        assertThat(DiskState.parseDiskState("Unknown")).isEqualTo(DiskState.UNKNOWN);
    }

    @Test
    public void parseReplStateRoundTrip()
    {
        for (ReplState replState : ReplState.values())
        {
            assertThat(ReplState.parseReplState(replState.toString())).isEqualTo(replState);
        }
    }

    @Test
    public void parseReplStateUnknownLabel()
    {
        assertThat(ReplState.parseReplState("NoSuchState")).isEqualTo(ReplState.UNKNOWN);
        assertThat(ReplState.parseReplState("synctarget")).isEqualTo(ReplState.UNKNOWN);
    }

    @Test
    public void parseDoneValidPercentage() throws EventsSourceException
    {
        assertThat(ReplState.parseDone("0")).isEqualTo(0.0f);
        assertThat(ReplState.parseDone("12.34")).isEqualTo(12.34f);
        assertThat(ReplState.parseDone("100")).isEqualTo(100.0f);
    }

    @Test
    public void parseDoneClampsOutOfRangeValues() throws EventsSourceException
    {
        assertThat(ReplState.parseDone("150.5")).isEqualTo(100.0f);
        assertThat(ReplState.parseDone("-3")).isEqualTo(0.0f);
    }

    @Test
    public void parseDoneUnparsableThrows()
    {
        assertThatThrownBy(() -> ReplState.parseDone("abc"))
            .isInstanceOf(EventsSourceException.class)
            .hasMessageContaining("not a parsable number");
    }
}
