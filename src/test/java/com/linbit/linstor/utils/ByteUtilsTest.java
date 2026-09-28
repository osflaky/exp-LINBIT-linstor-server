package com.linbit.linstor.utils;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ByteUtilsTest
{
    @Test
    public void testChecksumSha256KnownVectors()
    {
        // Standard SHA-256 test vectors
        assertEquals(
            "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
            ByteUtils.bytesToHex(ByteUtils.checksumSha256(new byte[0]))
        );
        assertEquals(
            "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD",
            ByteUtils.bytesToHex(ByteUtils.checksumSha256("abc".getBytes(StandardCharsets.US_ASCII)))
        );
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testChecksumSha256Length()
    {
        assertEquals(32, ByteUtils.checksumSha256("some data".getBytes(StandardCharsets.US_ASCII)).length);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testBytesToHex()
    {
        assertEquals("", ByteUtils.bytesToHex(new byte[0]));
        assertEquals("00", ByteUtils.bytesToHex(new byte[] {0x00}));
        assertEquals("000FFF7A", ByteUtils.bytesToHex(new byte[] {0x00, 0x0F, (byte) 0xFF, 0x7A}));
        assertEquals("80", ByteUtils.bytesToHex(new byte[] {(byte) 0x80}));
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testHexToBytes()
    {
        assertArrayEquals(new byte[0], ByteUtils.hexToBytes(""));
        assertArrayEquals(new byte[] {0x00}, ByteUtils.hexToBytes("00"));
        assertArrayEquals(new byte[] {0x00, 0x0F, (byte) 0xFF, 0x7A}, ByteUtils.hexToBytes("000FFF7A"));
        // Lower-case digits are accepted as well
        assertArrayEquals(new byte[] {0x0F, (byte) 0xFF, 0x7A}, ByteUtils.hexToBytes("0fff7a"));
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testHexToBytesOddLengthDropsTrailingNibble()
    {
        // Surprising behavior: for input of odd length the last hex digit is silently ignored
        assertArrayEquals(new byte[] {(byte) 0xAB}, ByteUtils.hexToBytes("ABC"));
        assertArrayEquals(new byte[0], ByteUtils.hexToBytes("A"));
    }

    @Test
    public void testHexToBytesInvalidCharacter()
    {
        try
        {
            ByteUtils.hexToBytes("G0");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException exc)
        {
            assertEquals("Invalid string passed to method hexToBytes: \"G0\"", exc.getMessage());
        }
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testHexRoundTrip()
    {
        byte[] data = new byte[] {0x00, 0x01, 0x7F, (byte) 0x80, (byte) 0xFE, (byte) 0xFF, 0x42};
        assertArrayEquals(data, ByteUtils.hexToBytes(ByteUtils.bytesToHex(data)));
    }
}
