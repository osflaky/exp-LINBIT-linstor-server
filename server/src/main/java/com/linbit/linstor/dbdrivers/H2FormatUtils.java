package com.linbit.linstor.dbdrivers;

import com.linbit.linstor.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Helpers to detect H2 database files written by H2 1.x, which H2 2.x can neither read nor
 * migrate in place. Used by the controller startup guard, "linstor-database migrate-h2" and the
 * error-report database rotation.
 */
public final class H2FormatUtils
{
    public static final String MV_DB_SUFFIX = ".mv.db";
    public static final String PAGESTORE_DB_SUFFIX = ".h2.db";
    public static final String LEGACY_BACKUP_SUFFIX = ".h2v1.bak";

    private static final String JDBC_H2_PREFIX = "jdbc:h2:";
    private static final String FILE_PREFIX = "file:";
    private static final String[] NON_FILE_SUBPROTOCOLS = {"mem:", "tcp:", "ssl:", "zip:"};

    /*
     * The MVStore file header is a plain-text key:value block within the first bytes of the file,
     * e.g. "H:2,blockSize:1000,created:...,format:1,fletcher:...". H2 1.x writes "format:1",
     * H2 2.x writes "format:2".
     */
    private static final int HEADER_SNIFF_LEN = 4096;
    private static final String LEGACY_FORMAT_MARKER = ",format:1,";

    private H2FormatUtils()
    {
    }

    /**
     * Derives the database base path (without the ".mv.db" / ".h2.db" suffix) from a jdbc:h2 URL.
     *
     * @return the base path, or null if the URL is not a file-based H2 URL (mem:, tcp:, ssl:, zip:)
     */
    public static @Nullable Path h2DbBasePathFromUrl(String connectionUrl)
    {
        @Nullable Path result = null;
        if (connectionUrl.toLowerCase(Locale.ROOT).startsWith(JDBC_H2_PREFIX))
        {
            String path = connectionUrl.substring(JDBC_H2_PREFIX.length());
            int optionsIdx = path.indexOf(';');
            if (optionsIdx >= 0)
            {
                path = path.substring(0, optionsIdx);
            }
            if (path.toLowerCase(Locale.ROOT).startsWith(FILE_PREFIX))
            {
                path = path.substring(FILE_PREFIX.length());
            }
            boolean nonFile = path.isEmpty();
            for (String subProtocol : NON_FILE_SUBPROTOCOLS)
            {
                nonFile |= path.toLowerCase(Locale.ROOT).startsWith(subProtocol);
            }
            if (!nonFile)
            {
                result = Paths.get(path);
            }
        }
        return result;
    }

    /**
     * Checks if the given ".mv.db" file was written by H2 1.x (MVStore file format 1).
     */
    public static boolean isLegacyMvDbFormat(Path mvDbFile) throws IOException
    {
        byte[] header = new byte[HEADER_SNIFF_LEN];
        int read = 0;
        try (InputStream in = Files.newInputStream(mvDbFile))
        {
            int len;
            while (read < HEADER_SNIFF_LEN && (len = in.read(header, read, HEADER_SNIFF_LEN - read)) != -1)
            {
                read += len;
            }
        }
        return new String(header, 0, read, StandardCharsets.ISO_8859_1).contains(LEGACY_FORMAT_MARKER);
    }

    /**
     * Finds a database file for the given jdbc:h2 URL that the currently loaded H2 driver cannot open,
     * i.e. a format-1 ".mv.db" or a legacy PageStore ".h2.db" (without a corresponding ".mv.db").
     *
     * @return the legacy database file, or null if there is nothing to migrate
     */
    public static @Nullable Path findLegacyH2DbFile(String connectionUrl) throws IOException
    {
        @Nullable Path result = null;
        @Nullable Path basePath = h2DbBasePathFromUrl(connectionUrl);
        if (basePath != null)
        {
            Path mvDbFile = Paths.get(basePath + MV_DB_SUFFIX);
            Path pageStoreFile = Paths.get(basePath + PAGESTORE_DB_SUFFIX);
            if (Files.isRegularFile(mvDbFile))
            {
                if (isLegacyMvDbFormat(mvDbFile))
                {
                    result = mvDbFile;
                }
            }
            else if (Files.isRegularFile(pageStoreFile))
            {
                // H2 2.x silently ignores PageStore files and would create a fresh empty database
                result = pageStoreFile;
            }
        }
        return result;
    }

    /**
     * Major version of the H2 driver loaded by the current classloader. Uses a virtual call on the
     * driver instance instead of org.h2.engine.Constants, since those compile-time constants would
     * be inlined by the compiler and always report the build-time version.
     */
    public static int loadedH2MajorVersion()
    {
        return new org.h2.Driver().getMajorVersion();
    }
}
