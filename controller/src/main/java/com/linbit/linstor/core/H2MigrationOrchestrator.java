package com.linbit.linstor.core;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.cfg.CtrlConfig;
import com.linbit.linstor.dbdrivers.H2FormatUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Migrates a database written by H2 1.x to the H2 2.x file format, since H2 2.x can neither read
 * nor upgrade 1.x files in place.
 * <p>
 * The old database is read with the original H2 1.4 driver, which is shipped as
 * <code>lib/migration/h2-*.jar</code> outside of the regular classpath. Both migration phases run
 * in child JVMs so that the shipped export-db / import-db code paths are reused unchanged:
 * <ol>
 * <li>export-db with the old H2 jar prepended to the classpath (the old driver shadows the new
 * one), migrating the old database to the current schema version first</li>
 * <li>rename the old database file to *.h2v1.bak</li>
 * <li>import-db with the regular classpath, creating a fresh database with the new driver</li>
 * </ol>
 * On failure the original database file is restored.
 * <p>
 * Used by "linstor-database migrate-h2" and by the controller at startup.
 */
public class H2MigrationOrchestrator
{
    public enum MigrationCheck
    {
        NOT_NEEDED,
        NEEDED,
        DB_IN_USE,
        INTERRUPTED
    }

    private static final String EXPORT_FILE_SUFFIX = ".h2migration.json";
    private static final String MIGRATION_JAR_DIR = "migration";
    private static final String MIGRATION_JAR_GLOB = "h2-*.jar";
    private static final long REQUIRED_FREE_SPACE_FACTOR = 3;

    private final String configurationDirectory;
    private final CtrlConfig ctrlCfg;
    private final PrintStream out;
    private final PrintStream errOut;

    public H2MigrationOrchestrator(
        String configurationDirectoryRef,
        @Nullable String logDirectoryRef,
        PrintStream outRef,
        PrintStream errRef
    )
    {
        configurationDirectory = configurationDirectoryRef;
        List<String> cfgArgs = new ArrayList<>(Arrays.asList("-c", configurationDirectoryRef));
        if (logDirectoryRef != null)
        {
            cfgArgs.add("--logs");
            cfgArgs.add(logDirectoryRef);
        }
        ctrlCfg = new CtrlConfig(cfgArgs.toArray(new String[0]));
        out = outRef;
        errOut = errRef;
    }

    /**
     * Checks whether the configured database needs (or currently allows) a migration.
     */
    public MigrationCheck check() throws IOException
    {
        MigrationCheck result = MigrationCheck.NOT_NEEDED;
        @Nullable String dbUrl = getFileBasedH2Url();
        if (dbUrl != null)
        {
            @Nullable Path basePath = H2FormatUtils.h2DbBasePathFromUrl(dbUrl);
            @Nullable Path legacyDbFile = H2FormatUtils.findLegacyH2DbFile(dbUrl);
            if (legacyDbFile != null)
            {
                result = isDbFileInUse(legacyDbFile) ? MigrationCheck.DB_IN_USE : MigrationCheck.NEEDED;
            }
            else if (basePath != null && isInterruptedMigration(basePath))
            {
                result = MigrationCheck.INTERRUPTED;
            }
        }
        return result;
    }

    /**
     * Runs the actual migration. Expects {@link #check()} to have returned {@link MigrationCheck#NEEDED}.
     *
     * @return true if the database was migrated successfully
     */
    public boolean migrate(@Nullable Path h2JarOverride)
    {
        boolean success = false;
        try
        {
            String dbUrl = getFileBasedH2Url();
            if (dbUrl == null)
            {
                throw new IOException("The configured database is not a file-based H2 database: " + dbUrl);
            }
            @Nullable Path legacyDbFile = H2FormatUtils.findLegacyH2DbFile(dbUrl);
            if (legacyDbFile == null)
            {
                throw new IOException("No H2 1.x database file found for: " + dbUrl);
            }

            checkFreeSpace(legacyDbFile);
            Path oldH2Jar = h2JarOverride != null ? h2JarOverride : findMigrationJar();
            if (!Files.isRegularFile(oldH2Jar))
            {
                throw new IOException("H2 1.x driver jar not found: " + oldH2Jar);
            }

            Path exportFile = legacyDbFile.resolveSibling(legacyDbFile.getFileName() + EXPORT_FILE_SUFFIX);
            Path backupFile = legacyDbFile.resolveSibling(
                legacyDbFile.getFileName() + H2FormatUtils.LEGACY_BACKUP_SUFFIX
            );
            @Nullable Path basePath = H2FormatUtils.h2DbBasePathFromUrl(dbUrl);
            Path newDbFile = Paths.get(basePath + H2FormatUtils.MV_DB_SUFFIX);

            try
            {
                createPrivateFile(exportFile);

                out.println("Exporting database using the H2 1.x driver " + oldH2Jar.getFileName() + " ...");
                runChildJvm(
                    oldH2Jar + java.io.File.pathSeparator + System.getProperty("java.class.path"),
                    "export-db",
                    "--migrate-before-export",
                    "-c",
                    configurationDirectory,
                    "--logs",
                    ctrlCfg.getLogDirectory(),
                    exportFile.toString()
                );

                Files.move(legacyDbFile, backupFile);
                out.println("Moved " + legacyDbFile + " to " + backupFile);

                try
                {
                    out.println("Importing database using the H2 " + H2FormatUtils.loadedH2MajorVersion() +
                        ".x driver ...");
                    runChildJvm(
                        System.getProperty("java.class.path"),
                        "import-db",
                        "-c",
                        configurationDirectory,
                        "--logs",
                        ctrlCfg.getLogDirectory(),
                        exportFile.toString()
                    );
                    success = true;
                    out.println("Database successfully migrated to the new H2 format.");
                    out.println("The original database was kept as " + backupFile +
                        " and can be deleted once the new database is confirmed working.");
                }
                catch (IOException exc)
                {
                    errOut.println("Import into the new database failed, restoring the original database file");
                    Files.deleteIfExists(newDbFile);
                    Files.move(backupFile, legacyDbFile);
                    throw exc;
                }
            }
            finally
            {
                Files.deleteIfExists(exportFile);
            }
        }
        catch (IOException | RuntimeException exc)
        {
            errOut.println("H2 database migration failed: " + exc.getMessage());
        }
        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();
            errOut.println("H2 database migration was interrupted");
        }
        return success;
    }

    public @Nullable String getFileBasedH2Url()
    {
        @Nullable String result = null;
        if (ctrlCfg.getDbInMemory() == null)
        {
            String dbUrl = ctrlCfg.getDbConnectionUrl();
            if (H2FormatUtils.h2DbBasePathFromUrl(dbUrl) != null)
            {
                result = dbUrl;
            }
        }
        return result;
    }

    private boolean isInterruptedMigration(Path basePath)
    {
        Path mvDbFile = Paths.get(basePath + H2FormatUtils.MV_DB_SUFFIX);
        Path mvDbBackup = Paths.get(basePath + H2FormatUtils.MV_DB_SUFFIX + H2FormatUtils.LEGACY_BACKUP_SUFFIX);
        Path pageStoreBackup = Paths.get(
            basePath + H2FormatUtils.PAGESTORE_DB_SUFFIX + H2FormatUtils.LEGACY_BACKUP_SUFFIX
        );
        return !Files.exists(mvDbFile) && (Files.exists(mvDbBackup) || Files.exists(pageStoreBackup));
    }

    /**
     * A running H2 instance (usually a running controller) holds a file lock on the database file.
     */
    private boolean isDbFileInUse(Path dbFile)
    {
        boolean inUse = false;
        try (FileChannel channel = FileChannel.open(dbFile, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            @Nullable FileLock lock = channel.tryLock();
            if (lock == null)
            {
                inUse = true;
            }
            else
            {
                lock.release();
            }
        }
        catch (IOException ignored)
        {
            // if the probe itself fails, let the actual migration produce the real error message
        }
        return inUse;
    }

    private void checkFreeSpace(Path dbFile) throws IOException
    {
        long dbSize = Files.size(dbFile);
        long usable = Files.getFileStore(dbFile.toAbsolutePath().getParent()).getUsableSpace();
        if (usable < dbSize * REQUIRED_FREE_SPACE_FACTOR)
        {
            throw new IOException(
                "Not enough free space for the database migration: " + usable + " bytes available, " +
                    dbSize * REQUIRED_FREE_SPACE_FACTOR + " bytes (" + REQUIRED_FREE_SPACE_FACTOR +
                    "x database size) required in " + dbFile.toAbsolutePath().getParent()
            );
        }
    }

    /**
     * Locates lib/migration/h2-*.jar relative to the jar this class was loaded from.
     */
    private Path findMigrationJar() throws IOException
    {
        @Nullable CodeSource codeSource = LinstorDatabaseTool.class.getProtectionDomain().getCodeSource();
        if (codeSource == null)
        {
            throw new IOException("Unable to determine the installation directory, use --h2-jar");
        }
        Path selfPath;
        try
        {
            selfPath = Paths.get(codeSource.getLocation().toURI());
        }
        catch (URISyntaxException exc)
        {
            throw new IOException("Unable to determine the installation directory, use --h2-jar", exc);
        }
        Path migrationDir = selfPath.getParent().resolve(MIGRATION_JAR_DIR);
        @Nullable Path jar = null;
        if (Files.isDirectory(migrationDir))
        {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(migrationDir, MIGRATION_JAR_GLOB))
            {
                for (Path entry : dirStream)
                {
                    jar = entry;
                    break;
                }
            }
        }
        if (jar == null)
        {
            throw new IOException(
                "H2 1.x driver jar not found in " + migrationDir + ", use --h2-jar to specify its location"
            );
        }
        return jar;
    }

    /**
     * The export file contains all LINSTOR data including encrypted secrets, restrict it to the owner.
     */
    private void createPrivateFile(Path file) throws IOException
    {
        Files.deleteIfExists(file);
        try
        {
            Files.createFile(
                file,
                PosixFilePermissions.asFileAttribute(
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                )
            );
        }
        catch (UnsupportedOperationException ignored)
        {
            Files.createFile(file);
        }
    }

    private void runChildJvm(String classPath, String... arguments) throws IOException, InterruptedException
    {
        Path javaBin = Paths.get(System.getProperty("java.home"), "bin", "java");
        List<String> command = new ArrayList<>();
        command.add(javaBin.toString());
        command.add("-cp");
        command.add(classPath);
        command.add(LinstorDatabaseTool.class.getName());
        for (String argument : arguments)
        {
            command.add(argument);
        }

        Process process = new ProcessBuilder(command)
            .inheritIO()
            .start();
        int exitCode = process.waitFor();
        if (exitCode != 0)
        {
            throw new IOException(
                "'linstor-database " + arguments[0] + "' exited with code " + exitCode
            );
        }
    }
}
