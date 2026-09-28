package com.linbit.linstor.layer.storage.spdk.utils;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpdkUtilsTest
{
    @Test
    public void parseNvmeDrivesAddressesFindsNvmeDevices()
    {
        // lspci -mm -n -D output: NVMe drives are class 0108 with prog-if 02
        String lspciOutput = """
            0000:00:00.0 "0600" "8086" "29c0" -r02 "1af4" "1100"
            0000:00:01.0 "0300" "1234" "1111" -r02 "1af4" "1100"
            0000:00:04.0 "0108" "8086" "0953" -p02 "8086" "3702"
            0000:00:1f.2 "0106" "8086" "2922" -r02 -p01 "1af4" "1100"
            0000:01:00.0 "0108" "144d" "a808" -p02 "144d" "a801"
            """;

        List<String> addresses = SpdkUtils.parseNvmeDrivesAddresses(lspciOutput);

        assertThat(addresses).containsExactly("0000:00:04.0", "0000:01:00.0");
    }

    @Test
    public void parseNvmeDrivesAddressesIgnoresOtherProgIf()
    {
        // class 0108 but prog-if 01 must not match
        String lspciOutput = "0000:00:04.0 \"0108\" \"8086\" \"0953\" -p01 \"8086\" \"3702\"\n";

        assertThat(SpdkUtils.parseNvmeDrivesAddresses(lspciOutput)).isEmpty();
    }

    @Test
    public void parseNvmeDrivesAddressesEmptyOutput()
    {
        assertThat(SpdkUtils.parseNvmeDrivesAddresses("")).isEmpty();
    }

    @Test
    public void parseNvmeDrivesAddressesTrimsLeadingWhitespace()
    {
        String lspciOutput = "  0000:00:04.0 \"0108\" \"8086\" \"0953\" -p02 \"8086\" \"3702\"\n";

        assertThat(SpdkUtils.parseNvmeDrivesAddresses(lspciOutput)).containsExactly("0000:00:04.0");
    }
}
