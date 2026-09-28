package com.linbit.linstor.layer.storage.utils;

import com.linbit.extproc.ExtCmd.OutputData;
import com.linbit.linstor.storage.StorageException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParseUtilsTest
{
    private static OutputData outputOf(String stdout)
    {
        return new OutputData(
            new String[]{"test-cmd"},
            stdout.getBytes(StandardCharsets.UTF_8),
            new byte[0],
            0
        );
    }

    // --- parseSimpleTable(output, delimiter, descr) (2-arg key=0, value=1) ---

    @Test
    public void testParseSimpleTableBasic() throws StorageException
    {
        OutputData output = outputOf("alpha;100\nbeta;200\ngamma;300\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(3, result.size());
        assertEquals(100L, (long) result.get("alpha"));
        assertEquals(200L, (long) result.get("beta"));
        assertEquals(300L, (long) result.get("gamma"));
    }

    @Test
    public void testParseSimpleTableBasicNoTrail() throws StorageException
    {
        OutputData output = outputOf("alpha;100\nbeta;200\ngamma;300");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(3, result.size());
        assertEquals(100L, (long) result.get("alpha"));
        assertEquals(200L, (long) result.get("beta"));
        assertEquals(300L, (long) result.get("gamma"));
    }

    @Test
    public void testParseSimpleTableEmptyOutput() throws StorageException
    {
        OutputData output = outputOf("");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseSimpleTableSingleLine() throws StorageException
    {
        OutputData output = outputOf("foo;42\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(42L, (long) result.get("foo"));
    }

    @Test
    public void testParseSimpleTableExtraColumns() throws StorageException
    {
        OutputData output = outputOf("key;999;extra;columns\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(999L, (long) result.get("key"));
    }

    @Test
    public void testParseSimpleTableDecimalValue() throws StorageException
    {
        OutputData output = outputOf("vol;123.456\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(123L, (long) result.get("vol"));
    }

    @Test
    public void testParseSimpleTableCommaDecimal() throws StorageException
    {
        OutputData output = outputOf("vol;123,456\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(123L, (long) result.get("vol"));
    }

    @Test
    public void testParseSimpleTableWhitespaceLines() throws StorageException
    {
        OutputData output = outputOf("  key;50  \n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(50L, (long) result.get("key"));
    }

    // --- parseSimpleTable(output, delimiter, descr, keyColumnIndex, valueColumnIndex) ---

    @Test
    public void testParseSimpleTableCustomColumns() throws StorageException
    {
        OutputData output = outputOf("ignore;thekey;500;extra\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test", 1, 2);

        assertEquals(1, result.size());
        assertEquals(500L, (long) result.get("thekey"));
    }

    @Test
    public void testParseSimpleTableReversedColumns() throws StorageException
    {
        OutputData output = outputOf("100;mykey\n200;other\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test", 1, 0);

        assertEquals(2, result.size());
        assertEquals(100L, (long) result.get("mykey"));
        assertEquals(200L, (long) result.get("other"));
    }

    @Test
    public void testParseSimpleTableDuplicateKeysLastWins() throws StorageException
    {
        OutputData output = outputOf("dup;10\ndup;20\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, ";", "test");

        assertEquals(1, result.size());
        assertEquals(20L, (long) result.get("dup"));
    }

    @Test
    public void testParseSimpleTableTabDelimiter() throws StorageException
    {
        OutputData output = outputOf("name\t1024\nother\t2048\n");
        Map<String, Long> result = ParseUtils.parseSimpleTable(output, "\t", "test");

        assertEquals(2, result.size());
        assertEquals(1024L, (long) result.get("name"));
        assertEquals(2048L, (long) result.get("other"));
    }

    // --- error cases ---

    @Test(expected = StorageException.class)
    public void testParseSimpleTableTooFewColumns() throws StorageException
    {
        OutputData output = outputOf("onlyonecolumn\n");
        ParseUtils.parseSimpleTable(output, ";", "test");
    }

    @Test(expected = StorageException.class)
    public void testParseSimpleTableNonNumericValue() throws StorageException
    {
        OutputData output = outputOf("key;notanumber\n");
        ParseUtils.parseSimpleTable(output, ";", "test");
    }

    @Test(expected = StorageException.class)
    public void testParseSimpleTableTooFewColumnsForCustomIndex() throws StorageException
    {
        OutputData output = outputOf("a;b\n");
        ParseUtils.parseSimpleTable(output, ";", "test", 0, 5);
    }
}
