package com.linbit.linstor.dbcp.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2026_02_16_RenameExtCmdWaitToTest extends MigrationTestBase
{
    private static final String PRE_VERSION = "2026.01.30.09.00";
    private static final String TEST_VERSION = "2026.02.16.09.00";

    private static final String OLD_KEY = "ExtCmdWaitTimeout";
    private static final String NEW_KEY = "ExtCmd/WaitTimeout";

    @Test
    public void renamesPropertyKeyOnAllInstances() throws Exception
    {
        startDbAtVersion("renameExtCmdWaitTo", PRE_VERSION);

        executeUpdate(
            "INSERT INTO PROPS_CONTAINERS (PROPS_INSTANCE, PROP_KEY, PROP_VALUE) VALUES" +
                " ('/CTRL', '" + OLD_KEY + "', '45000')," +
                " ('/NODES/NODE1', '" + OLD_KEY + "', '30000')," +
                // key that merely starts with the old key must not be renamed
                " ('/CTRL', '" + OLD_KEY + "Extra', 'unrelated')"
        );

        migrateTo(TEST_VERSION);

        // old key is gone on all instances
        assertThat(countRows("PROPS_CONTAINERS WHERE PROP_KEY = '" + OLD_KEY + "'")).isZero();

        // values are preserved under the new key
        assertThat(querySingleString(selectProp("/CTRL", NEW_KEY))).isEqualTo("45000");
        assertThat(querySingleString(selectProp("/NODES/NODE1", NEW_KEY))).isEqualTo("30000");

        // similarly named but different keys are untouched
        assertThat(querySingleString(selectProp("/CTRL", OLD_KEY + "Extra"))).isEqualTo("unrelated");
    }

    private String selectProp(String instance, String key)
    {
        return "SELECT PROP_VALUE FROM PROPS_CONTAINERS WHERE PROPS_INSTANCE = '" + instance +
            "' AND PROP_KEY = '" + key + "'";
    }
}
