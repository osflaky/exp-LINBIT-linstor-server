package com.linbit.linstor.dbcp.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2026_04_08_FixAutoBlockSizePropertyTest extends MigrationTestBase
{
    private static final String PRE_VERSION = "2026.03.18.09.00";
    private static final String TEST_VERSION = "2026.04.08.09.00";

    private static final String PROP_KEY = "Linstor/Drbd/auto-block-size";

    @Test
    public void movesPropertyFromStltToCtrl() throws Exception
    {
        startDbAtVersion("fixAutoBlockSizeMove", PRE_VERSION);

        executeUpdate(
            "INSERT INTO PROPS_CONTAINERS (PROPS_INSTANCE, PROP_KEY, PROP_VALUE) VALUES" +
                " ('/STLT', '" + PROP_KEY + "', 'yes')," +
                // unrelated satellite level property must stay on /STLT
                " ('/STLT', 'SomeOtherProp', 'someValue')"
        );

        migrateTo(TEST_VERSION);

        assertThat(countRows(stltPropRows())).isZero();
        assertThat(querySingleString(selectProp("/CTRL", PROP_KEY))).isEqualTo("yes");
        assertThat(querySingleString(selectProp("/STLT", "SomeOtherProp"))).isEqualTo("someValue");
    }

    @Test
    public void deletesStltPropertyWhenCtrlPropertyExists() throws Exception
    {
        startDbAtVersion("fixAutoBlockSizeBoth", PRE_VERSION);

        executeUpdate(
            "INSERT INTO PROPS_CONTAINERS (PROPS_INSTANCE, PROP_KEY, PROP_VALUE) VALUES" +
                " ('/STLT', '" + PROP_KEY + "', 'no')," +
                " ('/CTRL', '" + PROP_KEY + "', 'yes')"
        );

        migrateTo(TEST_VERSION);

        // the correctly placed /CTRL value wins, the /STLT leftover is deleted
        assertThat(countRows(stltPropRows())).isZero();
        assertThat(querySingleString(selectProp("/CTRL", PROP_KEY))).isEqualTo("yes");
    }

    @Test
    public void keepsCtrlOnlyPropertyUntouched() throws Exception
    {
        startDbAtVersion("fixAutoBlockSizeCtrlOnly", PRE_VERSION);

        executeUpdate(
            "INSERT INTO PROPS_CONTAINERS (PROPS_INSTANCE, PROP_KEY, PROP_VALUE)" +
                " VALUES ('/CTRL', '" + PROP_KEY + "', 'yes')"
        );

        migrateTo(TEST_VERSION);

        assertThat(querySingleString(selectProp("/CTRL", PROP_KEY))).isEqualTo("yes");
        assertThat(countRows(stltPropRows())).isZero();
        assertThat(countRows("PROPS_CONTAINERS WHERE PROP_KEY = '" + PROP_KEY + "'")).isEqualTo(1);
    }

    private String stltPropRows()
    {
        return "PROPS_CONTAINERS WHERE PROPS_INSTANCE = '/STLT' AND PROP_KEY = '" + PROP_KEY + "'";
    }

    private String selectProp(String instance, String key)
    {
        return "SELECT PROP_VALUE FROM PROPS_CONTAINERS WHERE PROPS_INSTANCE = '" + instance +
            "' AND PROP_KEY = '" + key + "'";
    }
}
