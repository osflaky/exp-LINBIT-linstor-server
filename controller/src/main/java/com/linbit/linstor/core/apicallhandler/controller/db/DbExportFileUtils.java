package com.linbit.linstor.core.apicallhandler.controller.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Helper for reading and writing (optionally gzip compressed) database export files.
 * <p>
 * Writing compresses whenever the target file name ends with {@link #GZIP_SUFFIX}. Reading does not care about the
 * file name at all but detects gzip by its magic bytes, so that a compressed export can be imported no matter what
 * it was renamed to.
 * </p>
 */
public final class DbExportFileUtils
{
    public static final String GZIP_SUFFIX = ".gz";

    private static final int GZIP_MAGIC_BYTE_0 = 0x1f;
    private static final int GZIP_MAGIC_BYTE_1 = 0x8b;
    private static final int GZIP_MAGIC_LEN = 2;

    private DbExportFileUtils()
    {
    }

    /**
     * Checks whether the given path denotes a file that should be written gzip compressed.
     *
     * @return true if the given path's file name ends with {@link #GZIP_SUFFIX} (case-insensitive)
     */
    public static boolean hasGzipSuffix(Path pathRef)
    {
        Path fileName = pathRef.getFileName();
        return fileName != null && fileName.toString().toLowerCase(Locale.ROOT).endsWith(GZIP_SUFFIX);
    }

    /**
     * Opens an {@link OutputStream} to the given file. The stream is gzip compressed if the file name ends with
     * {@link #GZIP_SUFFIX}.
     */
    public static OutputStream newExportOutputStream(Path targetRef) throws IOException
    {
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetRef));
        if (hasGzipSuffix(targetRef))
        {
            out = new GZIPOutputStream(out);
        }
        return out;
    }

    /**
     * Opens an {@link InputStream} to the given file. If the file starts with the gzip magic bytes, the returned stream
     * transparently decompresses the content, regardless of the file name.
     */
    public static InputStream newExportInputStream(Path sourceRef) throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(Files.newInputStream(sourceRef));
        boolean gzip;
        try
        {
            in.mark(GZIP_MAGIC_LEN);
            int byte0 = in.read();
            int byte1 = in.read();
            in.reset();
            gzip = byte0 == GZIP_MAGIC_BYTE_0 && byte1 == GZIP_MAGIC_BYTE_1;
        }
        catch (IOException exc)
        {
            in.close();
            throw exc;
        }
        return gzip ? new GZIPInputStream(in) : in;
    }
}
