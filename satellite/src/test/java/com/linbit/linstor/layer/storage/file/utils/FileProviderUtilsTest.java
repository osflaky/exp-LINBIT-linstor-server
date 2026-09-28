package com.linbit.linstor.layer.storage.file.utils;

import com.linbit.extproc.ExtCmd.OutputData;
import com.linbit.linstor.layer.storage.file.utils.FileProviderUtils.FileInfo;
import com.linbit.linstor.storage.StorageException;
import com.linbit.utils.ExceptionThrowingFunction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileProviderUtilsTest
{
    private static final ExceptionThrowingFunction<String, Long, StorageException> ALLOC_SIZE = loPath -> 1024L;

    private static OutputData outputOf(String stdout)
    {
        return new OutputData(
            new String[]{"losetup"},
            stdout.getBytes(StandardCharsets.UTF_8),
            new byte[0],
            0
        );
    }

    private static Map<String, FileInfo> parse(String stdout) throws StorageException
    {
        return FileProviderUtils.parseLosetupList(outputOf(stdout), ALLOC_SIZE);
    }

    @Test
    public void testSingleDeviceWithTrailingNewline() throws StorageException
    {
        // Regression: losetup output ends with a newline. StringUtils.split preserves the
        // resulting trailing empty line, which must not trigger an ArrayIndexOutOfBoundsException.
        Map<String, FileInfo> result = parse(
            """
            NAME       BACK-FILE
            /dev/loop0 /var/lib/linstor/vol.img
            """
        );

        assertEquals(1, result.size());
        FileInfo info = result.get("/var/lib/linstor/vol.img");
        assertEquals("vol.img", info.identifier);
        assertEquals(Paths.get("/dev/loop0"), info.loPath);
        assertEquals(1024L, info.size);
    }

    @Test
    public void testMultipleDevices() throws StorageException
    {
        Map<String, FileInfo> result = parse(
            """
            NAME       BACK-FILE
            /dev/loop0 /var/lib/linstor/a.img
            /dev/loop1 /var/lib/linstor/b.img
            """
        );

        assertEquals(2, result.size());
        assertEquals("/dev/loop0", result.get("/var/lib/linstor/a.img").loPath.toString());
        assertEquals("/dev/loop1", result.get("/var/lib/linstor/b.img").loPath.toString());
    }

    @Test
    public void testOutputWithoutTrailingNewline() throws StorageException
    {
        Map<String, FileInfo> result = parse(
            "NAME       BACK-FILE\n" +
            "/dev/loop0 /var/lib/linstor/vol.img"
        );

        assertEquals(1, result.size());
        assertTrue(result.containsKey("/var/lib/linstor/vol.img"));
    }

    @Test
    public void testLoopDeviceWithoutBackingFileIsSkipped() throws StorageException
    {
        // A loop device with an empty BACK-FILE column produces a line that splits into a
        // single field; it must be skipped rather than crash.
        Map<String, FileInfo> result = parse(
            """
            NAME       BACK-FILE
            /dev/loop0 /var/lib/linstor/vol.img
            /dev/loop7
            """
        );

        assertEquals(1, result.size());
        assertTrue(result.containsKey("/var/lib/linstor/vol.img"));
    }

    @Test
    public void testHeaderOnly() throws StorageException
    {
        Map<String, FileInfo> result = parse("NAME       BACK-FILE\n");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyOutput() throws StorageException
    {
        Map<String, FileInfo> result = parse("");

        assertTrue(result.isEmpty());
    }
}
