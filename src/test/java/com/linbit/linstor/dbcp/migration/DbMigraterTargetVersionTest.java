package com.linbit.linstor.dbcp.migration;

import com.linbit.linstor.InitializationException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the resolution of the target version in DbMigrater#migrateToVersion, especially the cases where the
 * database is not empty - for example when running "import-db" without having deleted the old database
 * first. Such an import must fail with a clear error message when the database is already migrated beyond
 * the export's version (instead of the misleading "Target migration version 'X' does not exist" or, if no
 * migrations are pending at all, silently importing the old export into the newer schema).
 */
public class DbMigraterTargetVersionTest extends MigrationTestBase
{
    /**
     * An old migration version that is (and has to stay) part of the migration chain.
     */
    private static final String OLD_VERSION = "2023.03.22.10.00";
    private static final String UNKNOWN_VERSION = "2099.01.01.10.00";

    @Test
    public void migrateToOlderVersionThanDatabaseFails() throws Exception
    {
        startDbAtVersion("olderTargetVersion", null); // null: migrate to the latest version

        assertThatThrownBy(() -> migrateTo(OLD_VERSION))
            .isInstanceOf(InitializationException.class)
            .hasMessageContaining("already migrated")
            .hasMessageContaining(OLD_VERSION);
    }

    @Test
    public void migrateToCurrentDbVersionIsANoOp() throws Exception
    {
        startDbAtVersion("sameTargetVersion", OLD_VERSION);

        assertThatCode(() -> migrateTo(OLD_VERSION)).doesNotThrowAnyException();
    }

    @Test
    public void migrateToUnknownVersionFails() throws Exception
    {
        startDbAtVersion("unknownTargetVersion", OLD_VERSION);

        assertThatThrownBy(() -> migrateTo(UNKNOWN_VERSION))
            .isInstanceOf(InitializationException.class)
            .hasMessageContaining("does not exist");
    }
}
