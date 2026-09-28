package com.linbit.linstor.core.apicallhandler.controller.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class DbExportFileUtilsTest
{
    private static final byte[] CONTENT = "{\"hello\": \"world\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] GZIP_MAGIC = {(byte) 0x1f, (byte) 0x8b};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private Path path(String name)
    {
        return tmpFolder.getRoot().toPath().resolve(name);
    }

    private static void write(OutputStream out, byte[] data) throws Exception
    {
        try (OutputStream o = out)
        {
            o.write(data);
        }
    }

    private static byte[] readAll(InputStream in) throws Exception
    {
        try (InputStream i = in)
        {
            return i.readAllBytes();
        }
    }

    @Test
    public void hasGzipSuffixIsCaseInsensitive()
    {
        assertThat(DbExportFileUtils.hasGzipSuffix(path("export.json.gz"))).isTrue();
        assertThat(DbExportFileUtils.hasGzipSuffix(path("export.json.GZ"))).isTrue();
        assertThat(DbExportFileUtils.hasGzipSuffix(path("export.json"))).isFalse();
        assertThat(DbExportFileUtils.hasGzipSuffix(path("gz"))).isFalse();
    }

    @Test
    public void outputIsCompressedWhenNameEndsWithGz() throws Exception
    {
        Path file = path("export.json.gz");

        write(DbExportFileUtils.newExportOutputStream(file), CONTENT);

        byte[] raw = Files.readAllBytes(file);
        assertThat(raw).startsWith(GZIP_MAGIC);
        assertThat(readAll(new GZIPInputStream(Files.newInputStream(file)))).isEqualTo(CONTENT);
    }

    @Test
    public void outputIsPlainOtherwise() throws Exception
    {
        Path file = path("export.json");

        write(DbExportFileUtils.newExportOutputStream(file), CONTENT);

        assertThat(Files.readAllBytes(file)).isEqualTo(CONTENT);
    }

    @Test
    public void inputDecompressesGzipRegardlessOfFileName() throws Exception
    {
        // an admin may have renamed (or never suffixed) the compressed export
        Path file = path("renamed-export.json");
        write(new GZIPOutputStream(Files.newOutputStream(file)), CONTENT);

        assertThat(readAll(DbExportFileUtils.newExportInputStream(file))).isEqualTo(CONTENT);
    }

    @Test
    public void inputPassesPlainFileThroughRegardlessOfFileName() throws Exception
    {
        Path file = path("plain-but-named.json.gz");
        Files.write(file, CONTENT);

        assertThat(readAll(DbExportFileUtils.newExportInputStream(file))).isEqualTo(CONTENT);
    }

    @Test
    public void inputHandlesFilesShorterThanTheMagic() throws Exception
    {
        Path empty = path("empty.json");
        Files.write(empty, new byte[0]);
        assertThat(readAll(DbExportFileUtils.newExportInputStream(empty))).isEmpty();

        Path oneByte = path("one.json");
        Files.write(oneByte, new byte[] {(byte) 0x1f});
        assertThat(readAll(DbExportFileUtils.newExportInputStream(oneByte))).containsExactly(0x1f);
    }

    @Test
    public void jacksonRoundTripThroughCompressedFile() throws Exception
    {
        Path file = path("export.json.gz");
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> data = Map.of("linstorVersion", "1.35.0", "tables", 42);

        try (OutputStream out = DbExportFileUtils.newExportOutputStream(file))
        {
            om.writeValue(out, data);
        }
        Map<String, Object> read;
        try (InputStream in = DbExportFileUtils.newExportInputStream(file))
        {
            read = om.readValue(in, new TypeReference<Map<String, Object>>()
            {
            });
        }

        assertThat(Files.readAllBytes(file)).startsWith(GZIP_MAGIC);
        assertThat(read).isEqualTo(data);
    }
}
