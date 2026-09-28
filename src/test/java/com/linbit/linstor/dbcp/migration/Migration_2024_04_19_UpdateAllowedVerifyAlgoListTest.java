package com.linbit.linstor.dbcp.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2024_04_19_UpdateAllowedVerifyAlgoListTest extends MigrationTestBase
{
    private static final String PRE_VERSION = "2024.04.18.09.00";
    private static final String TEST_VERSION = "2024.04.19.10.00";

    private static final String KEY_VERIFY_ALGO_LIST = "DrbdOptions/auto-verify-algo-allowed-list";

    private static final String NEW_ALGO_LIST = "crct10dif;crc32c;sha384;sha512;sha256;sha1;md5;windrbd";

    @Test
    public void updatesCtrlAllowedVerifyAlgoList() throws Exception
    {
        startDbAtVersion("updateAllowedVerifyAlgoList", PRE_VERSION);

        // the initial schema ships the old default value on the controller properties
        assertThat(querySingleString(selectProp("/CTRLCFG", KEY_VERIFY_ALGO_LIST)))
            .startsWith("crct10dif-pclmul;");

        // the same property key on another props instance must not be touched
        executeUpdate(
            "INSERT INTO PROPS_CONTAINERS (PROPS_INSTANCE, PROP_KEY, PROP_VALUE)" +
                " VALUES ('/NODES/NODE1', '" + KEY_VERIFY_ALGO_LIST + "', 'md5-generic')"
        );

        migrateTo(TEST_VERSION);

        assertThat(querySingleString(selectProp("/CTRLCFG", KEY_VERIFY_ALGO_LIST))).isEqualTo(NEW_ALGO_LIST);

        // other instances keep their value
        assertThat(querySingleString(selectProp("/NODES/NODE1", KEY_VERIFY_ALGO_LIST))).isEqualTo("md5-generic");

        // unrelated controller properties are untouched
        assertThat(querySingleString(selectProp("/CTRLCFG", "DrbdOptions/auto-quorum"))).isEqualTo("io-error");
    }

    private String selectProp(String instance, String key)
    {
        return "SELECT PROP_VALUE FROM PROPS_CONTAINERS WHERE PROPS_INSTANCE = '" + instance +
            "' AND PROP_KEY = '" + key + "'";
    }
}
