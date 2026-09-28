package com.linbit.linstor.dbcp.migration;

import com.linbit.linstor.DatabaseInfo.DbProduct;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2023_03_22_DisableAutoResyncAfterTest extends MigrationTestBase
{
    private static final String TEST_VERSION = "2023.03.22.10.00";

    private static final String KEY_AUTO_RESYNC_AFTER = "DrbdOptions/auto-resync-after-disable";

    private static final String SELECT_AUTO_RESYNC_AFTER = "SELECT PROP_VALUE FROM PROPS_CONTAINERS" +
        " WHERE PROPS_INSTANCE = '/CTRLCFG' AND PROP_KEY = '" + KEY_AUTO_RESYNC_AFTER + "'";

    /*
     * NOTE: the version just before this migration is the consolidated init migration (2018.03.27.14.01).
     * DbMigrater refuses to *resume* from that version (DbMigraterConsolidatedVersions lists it as too old),
     * so unlike the other migration tests we cannot stop just before the migration, insert legacy data and
     * continue. Instead the "property already exists" case is verified by invoking the migration directly.
     */

    @Test
    public void insertsDisablePropertyWhenMissing() throws Exception
    {
        startDbAtVersion("disableAutoResyncAfterMissing", TEST_VERSION);

        assertThat(querySingleString(SELECT_AUTO_RESYNC_AFTER)).isEqualTo("true");

        // unrelated controller properties are untouched
        assertThat(
            querySingleString(
                "SELECT PROP_VALUE FROM PROPS_CONTAINERS" +
                    " WHERE PROPS_INSTANCE = '/CTRLCFG' AND PROP_KEY = 'DrbdOptions/auto-quorum'"
            )
        ).isEqualTo("io-error");
    }

    @Test
    public void keepsExistingPropertyValue() throws Exception
    {
        startDbAtVersion("disableAutoResyncAfterExisting", TEST_VERSION);

        // simulate a user having explicitly re-enabled the auto-resync-after feature
        executeUpdate(
            "UPDATE PROPS_CONTAINERS SET PROP_VALUE = 'false'" +
                " WHERE PROPS_INSTANCE = '/CTRLCFG' AND PROP_KEY = '" + KEY_AUTO_RESYNC_AFTER + "'"
        );

        // re-running the migration must neither overwrite the value nor insert a duplicate row
        new Migration_2023_03_22_DisableAutoResyncAfter().migrate(conn, DbProduct.H2);

        assertThat(querySingleString(SELECT_AUTO_RESYNC_AFTER)).isEqualTo("false");
        assertThat(countRows("PROPS_CONTAINERS WHERE PROP_KEY = '" + KEY_AUTO_RESYNC_AFTER + "'")).isEqualTo(1);
    }
}
