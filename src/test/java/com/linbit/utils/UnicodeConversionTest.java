package com.linbit.utils;

import com.linbit.utils.UnicodeConversion.InvalidSequenceException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Characterization tests for {@link UnicodeConversion}.
 *
 * The expectations in this test describe the actual current behavior of the class,
 * including some surprising cases in the UTF-16 to UTF-8 direction (see the comments
 * on the surrogate related tests).
 */
@RunWith(JUnitParamsRunner.class)
public class UnicodeConversionTest
{
    @Test
    public void testUtf8ToUtf16Ascii()
        throws InvalidSequenceException
    {
        byte[] input = "Hello, LINSTOR!".getBytes(StandardCharsets.US_ASCII);
        char[] converted = UnicodeConversion.utf8BytesToUtf16Chars(input, false);
        assertArrayEquals("Hello, LINSTOR!".toCharArray(), converted);
    }

    @Test
    public void testUtf8ToUtf16EmptyInput()
        throws InvalidSequenceException
    {
        assertEquals(0, UnicodeConversion.utf8BytesToUtf16Chars(new byte[0], false).length);
        assertEquals(0, UnicodeConversion.utf8BytesToUtf16Chars(new byte[0], true).length);
    }

    @Test
    @Parameters(method = "generateValidUtf8Sequences")
    public void testUtf8ToUtf16ValidSequences(Utf8DecodeCase data)
        throws InvalidSequenceException
    {
        assertArrayEquals(data.expected, UnicodeConversion.utf8BytesToUtf16Chars(data.input, false));
    }

    @Test
    @Parameters(method = "generateValidUtf8Sequences")
    public void testUtf8ToUtf16ValidSequencesSecureMode(Utf8DecodeCase data)
        throws InvalidSequenceException
    {
        // Secure mode only changes internal buffer wiping, not the conversion result
        assertArrayEquals(data.expected, UnicodeConversion.utf8BytesToUtf16Chars(data.input, true));
    }

    @Test
    public void testUtf8ToUtf16MixedSequenceLengths()
        throws InvalidSequenceException
    {
        // "a", U+00E4 (2 bytes), U+20AC (3 bytes), U+1F600 (4 bytes, surrogate pair)
        byte[] input = bytes(0x61, 0xC3, 0xA4, 0xE2, 0x82, 0xAC, 0xF0, 0x9F, 0x98, 0x80);
        char[] converted = UnicodeConversion.utf8BytesToUtf16Chars(input, false);
        assertEquals("a\u00E4\u20AC\uD83D\uDE00", new String(converted));
    }

    @Test(expected = InvalidSequenceException.class)
    @Parameters(method = "generateInvalidUtf8Sequences")
    public void testUtf8ToUtf16InvalidSequences(Utf8DecodeCase data)
        throws InvalidSequenceException
    {
        UnicodeConversion.utf8BytesToUtf16Chars(data.input, false);
    }

    @Test(expected = InvalidSequenceException.class)
    @Parameters(method = "generateInvalidUtf8Sequences")
    public void testUtf8ToUtf16InvalidSequencesSecureMode(Utf8DecodeCase data)
        throws InvalidSequenceException
    {
        UnicodeConversion.utf8BytesToUtf16Chars(data.input, true);
    }

    @Test
    public void testUtf16ToUtf8Ascii()
        throws InvalidSequenceException
    {
        byte[] converted = UnicodeConversion.utf16CharsToUtf8Bytes("Hello".toCharArray(), false);
        assertArrayEquals(bytes(0x48, 0x65, 0x6C, 0x6C, 0x6F), converted);
    }

    @Test
    public void testUtf16ToUtf8EmptyInput()
        throws InvalidSequenceException
    {
        assertEquals(0, UnicodeConversion.utf16CharsToUtf8Bytes(new char[0], false).length);
        assertEquals(0, UnicodeConversion.utf16CharsToUtf8Bytes(new char[0], true).length);
    }

    @Test
    @Parameters(method = "generateValidUtf16Sequences")
    public void testUtf16ToUtf8ValidSequences(Utf16EncodeCase data)
        throws InvalidSequenceException
    {
        assertArrayEquals(data.expected, UnicodeConversion.utf16CharsToUtf8Bytes(data.input, false));
    }

    @Test
    @Parameters(method = "generateValidUtf16Sequences")
    public void testUtf16ToUtf8ValidSequencesSecureMode(Utf16EncodeCase data)
        throws InvalidSequenceException
    {
        // Secure mode only changes internal buffer wiping, not the conversion result
        assertArrayEquals(data.expected, UnicodeConversion.utf16CharsToUtf8Bytes(data.input, true));
    }

    @Test(expected = InvalidSequenceException.class)
    @Parameters(method = "generateInvalidUtf16Sequences")
    public void testUtf16ToUtf8InvalidSequences(Utf16EncodeCase data)
        throws InvalidSequenceException
    {
        UnicodeConversion.utf16CharsToUtf8Bytes(data.input, false);
    }

    @Test(expected = InvalidSequenceException.class)
    @Parameters(method = "generateInvalidUtf16Sequences")
    public void testUtf16ToUtf8InvalidSequencesSecureMode(Utf16EncodeCase data)
        throws InvalidSequenceException
    {
        UnicodeConversion.utf16CharsToUtf8Bytes(data.input, true);
    }

    @Test
    public void testRoundTripForBasicMultilingualPlaneText()
        throws InvalidSequenceException
    {
        char[] original = "a\u00E4\u20AC\u07FF\u0800\uFFFF".toCharArray();
        byte[] utf8 = UnicodeConversion.utf16CharsToUtf8Bytes(original, false);
        char[] roundTripped = UnicodeConversion.utf8BytesToUtf16Chars(utf8, false);
        assertArrayEquals(original, roundTripped);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<Utf8DecodeCase> generateValidUtf8Sequences()
    {
        Utf8DecodeCase[] data = new Utf8DecodeCase[]
        {
            // 1-byte sequences
            new Utf8DecodeCase("NUL", bytes(0x00), chars(0x0000)),
            new Utf8DecodeCase("U+007F", bytes(0x7F), chars(0x007F)),
            // 2-byte sequences
            new Utf8DecodeCase("U+0080", bytes(0xC2, 0x80), chars(0x0080)),
            new Utf8DecodeCase("U+00E4", bytes(0xC3, 0xA4), chars(0x00E4)),
            new Utf8DecodeCase("U+07FF", bytes(0xDF, 0xBF), chars(0x07FF)),
            // 3-byte sequences
            new Utf8DecodeCase("U+0800", bytes(0xE0, 0xA0, 0x80), chars(0x0800)),
            new Utf8DecodeCase("U+20AC", bytes(0xE2, 0x82, 0xAC), chars(0x20AC)),
            new Utf8DecodeCase("U+D7FF", bytes(0xED, 0x9F, 0xBF), chars(0xD7FF)),
            new Utf8DecodeCase("U+E000", bytes(0xEE, 0x80, 0x80), chars(0xE000)),
            new Utf8DecodeCase("U+FFFF", bytes(0xEF, 0xBF, 0xBF), chars(0xFFFF)),
            // 4-byte sequences, decoded to UTF-16 surrogate pairs
            new Utf8DecodeCase("U+10000", bytes(0xF0, 0x90, 0x80, 0x80), chars(0xD800, 0xDC00)),
            new Utf8DecodeCase("U+1F600", bytes(0xF0, 0x9F, 0x98, 0x80), chars(0xD83D, 0xDE00)),
            new Utf8DecodeCase("U+10FFFF", bytes(0xF4, 0x8F, 0xBF, 0xBF), chars(0xDBFF, 0xDFFF))
        };
        return Arrays.asList(data);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<Utf8DecodeCase> generateInvalidUtf8Sequences()
    {
        Utf8DecodeCase[] data = new Utf8DecodeCase[]
        {
            new Utf8DecodeCase("lone continuation byte 0x80", bytes(0x80), null),
            new Utf8DecodeCase("lone continuation byte 0xBF", bytes(0xBF), null),
            new Utf8DecodeCase("invalid lead byte 0xF8", bytes(0xF8), null),
            new Utf8DecodeCase("invalid lead byte 0xFE", bytes(0xFE), null),
            new Utf8DecodeCase("invalid lead byte 0xFF", bytes(0xFF), null),
            new Utf8DecodeCase("truncated 2-byte sequence", bytes(0xC3), null),
            new Utf8DecodeCase("truncated 3-byte sequence", bytes(0xE2, 0x82), null),
            new Utf8DecodeCase("truncated 4-byte sequence", bytes(0xF0, 0x9F, 0x98), null),
            new Utf8DecodeCase("truncated sequence after valid ASCII", bytes(0x41, 0xC3), null),
            new Utf8DecodeCase("non-continuation byte in sequence", bytes(0xC3, 0x41), null),
            new Utf8DecodeCase("new lead byte instead of continuation", bytes(0xC3, 0xC3, 0xA4), null),
            new Utf8DecodeCase("overlong 2-byte NUL", bytes(0xC0, 0x80), null),
            new Utf8DecodeCase("overlong 2-byte U+007F", bytes(0xC1, 0xBF), null),
            new Utf8DecodeCase("overlong 3-byte U+07FF", bytes(0xE0, 0x9F, 0xBF), null),
            new Utf8DecodeCase("overlong 4-byte U+FFFF", bytes(0xF0, 0x8F, 0xBF, 0xBF), null),
            new Utf8DecodeCase("encoded surrogate U+D800", bytes(0xED, 0xA0, 0x80), null),
            new Utf8DecodeCase("encoded surrogate U+DFFF", bytes(0xED, 0xBF, 0xBF), null),
            new Utf8DecodeCase("code point above U+10FFFF", bytes(0xF4, 0x90, 0x80, 0x80), null)
        };
        return Arrays.asList(data);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<Utf16EncodeCase> generateValidUtf16Sequences()
    {
        Utf16EncodeCase[] data = new Utf16EncodeCase[]
        {
            // 1-byte results
            new Utf16EncodeCase("NUL", chars(0x0000), bytes(0x00)),
            new Utf16EncodeCase("U+007F", chars(0x007F), bytes(0x7F)),
            // 2-byte results
            new Utf16EncodeCase("U+0080", chars(0x0080), bytes(0xC2, 0x80)),
            new Utf16EncodeCase("U+00E4", chars(0x00E4), bytes(0xC3, 0xA4)),
            new Utf16EncodeCase("U+07FF", chars(0x07FF), bytes(0xDF, 0xBF)),
            // 3-byte results
            new Utf16EncodeCase("U+0800", chars(0x0800), bytes(0xE0, 0xA0, 0x80)),
            new Utf16EncodeCase("U+20AC", chars(0x20AC), bytes(0xE2, 0x82, 0xAC)),
            new Utf16EncodeCase("U+FFFF", chars(0xFFFF), bytes(0xEF, 0xBF, 0xBF)),
            // Surprising behavior: the surrogate detection uses the mask 0xFA00 instead of 0xFC00,
            // so surrogates that have bit 9 set escape detection and are encoded like regular
            // BMP characters (producing 3-byte sequences that encode a surrogate code point,
            // which UnicodeConversion itself rejects when decoding)
            new Utf16EncodeCase("undetected high surrogate U+DA00", chars(0xDA00), bytes(0xED, 0xA8, 0x80)),
            new Utf16EncodeCase("undetected high surrogate U+DBFF", chars(0xDBFF), bytes(0xED, 0xAF, 0xBF)),
            new Utf16EncodeCase("undetected low surrogate U+DE00", chars(0xDE00), bytes(0xED, 0xB8, 0x80)),
            // Both halves of the surrogate pair for U+10FFFF have bit 9 set, so instead of throwing
            // (like other surrogate pairs, see the invalid cases) the pair is encoded as two
            // separate 3-byte sequences
            new Utf16EncodeCase(
                "surrogate pair U+10FFFF encoded as two 3-byte sequences",
                chars(0xDBFF, 0xDFFF),
                bytes(0xED, 0xAF, 0xBF, 0xED, 0xBF, 0xBF)
            ),
            // Mixed multi-character input
            new Utf16EncodeCase(
                "mixed a U+00E4 U+20AC",
                chars(0x61, 0x00E4, 0x20AC),
                bytes(0x61, 0xC3, 0xA4, 0xE2, 0x82, 0xAC)
            )
        };
        return Arrays.asList(data);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<Utf16EncodeCase> generateInvalidUtf16Sequences()
    {
        // Surprising behavior: since the surrogate detection mask is 0xFA00 (bit 10 is not part of
        // the mask), the low surrogate continuation check "ctrlSeq == 0xDC00" can never be true.
        // As a result, even well-formed UTF-16 surrogate pairs are rejected: the high surrogate is
        // recognized and switches the state engine to CONT_UNIT, where every possible follow-up
        // character throws an InvalidSequenceException
        Utf16EncodeCase[] data = new Utf16EncodeCase[]
        {
            new Utf16EncodeCase("valid surrogate pair U+10000", chars(0xD800, 0xDC00), null),
            new Utf16EncodeCase("valid surrogate pair U+1F600", chars(0xD83D, 0xDE00), null),
            new Utf16EncodeCase("lone high surrogate at end of input", chars(0xD800), null),
            new Utf16EncodeCase("high surrogate followed by ASCII", chars(0xD800, 0x41), null),
            // U+DC00 has bit 9 clear, so (0xDC00 & 0xFA00) == 0xD800 and it is treated
            // like a high surrogate, leaving an incomplete sequence at the end of the input
            new Utf16EncodeCase("lone low surrogate U+DC00", chars(0xDC00), null)
        };
        return Arrays.asList(data);
    }

    private static byte[] bytes(int... values)
    {
        byte[] result = new byte[values.length];
        for (int idx = 0; idx < values.length; idx++)
        {
            result[idx] = (byte) values[idx];
        }
        return result;
    }

    private static char[] chars(int... values)
    {
        char[] result = new char[values.length];
        for (int idx = 0; idx < values.length; idx++)
        {
            result[idx] = (char) values[idx];
        }
        return result;
    }

    private static final class Utf8DecodeCase
    {
        final String description;
        final byte[] input;
        final char[] expected;

        Utf8DecodeCase(String descriptionRef, byte[] inputRef, char[] expectedRef)
        {
            description = descriptionRef;
            input = inputRef;
            expected = expectedRef;
        }

        @Override
        public String toString()
        {
            return description;
        }
    }

    private static final class Utf16EncodeCase
    {
        final String description;
        final char[] input;
        final byte[] expected;

        Utf16EncodeCase(String descriptionRef, char[] inputRef, byte[] expectedRef)
        {
            description = descriptionRef;
            input = inputRef;
            expected = expectedRef;
        }

        @Override
        public String toString()
        {
            return description;
        }
    }
}
