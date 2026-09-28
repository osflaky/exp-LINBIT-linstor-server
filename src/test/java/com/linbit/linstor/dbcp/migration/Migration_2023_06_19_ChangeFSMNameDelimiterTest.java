package com.linbit.linstor.dbcp.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2023_06_19_ChangeFSMNameDelimiterTest extends MigrationTestBase
{
    private static final String PRE_VERSION = "2023.06.12.10.00";
    private static final String TEST_VERSION = "2023.06.19.10.00";

    @Test
    public void changeFreeSpaceMgrNameDelimiter() throws Exception
    {
        startDbAtVersion("changeFsmNameDelimiter", PRE_VERSION);

        executeUpdate(
            "INSERT INTO NODES (UUID, NODE_NAME, NODE_DSP_NAME, NODE_FLAGS, NODE_TYPE)" +
                " VALUES ('10000000-0000-0000-0000-000000000001', 'NODE1', 'node1', 0, 2)"
        );
        executeUpdate(
            "INSERT INTO STOR_POOL_DEFINITIONS (UUID, POOL_NAME, POOL_DSP_NAME)" +
                " VALUES ('20000000-0000-0000-0000-000000000001', 'POOL1', 'pool1')"
        );
        // storage pool with the old ':' FSM delimiter, one without any delimiter and one with multiple colons
        executeUpdate(
            "INSERT INTO NODE_STOR_POOL" +
                " (UUID, NODE_NAME, POOL_NAME, DRIVER_NAME, FREE_SPACE_MGR_NAME, FREE_SPACE_MGR_DSP_NAME) VALUES" +
                " ('30000000-0000-0000-0000-000000000001', 'NODE1', 'POOL1', 'LvmDriver'," +
                "  'NODE1:POOL1', 'node1:pool1')," +
                " ('30000000-0000-0000-0000-000000000002', 'NODE1', 'DFLTSTORPOOL', 'LvmDriver'," +
                "  'NOCOLONFSM', 'NoColonFsm')," +
                " ('30000000-0000-0000-0000-000000000003', 'NODE1', 'DFLTDISKLESSSTORPOOL', 'DisklessDriver'," +
                "  'A:B:C', 'a:b:c')"
        );
        // object protection (and its ACL entry) of a free space manager, which must be deleted by the migration
        executeUpdate(
            "INSERT INTO SEC_OBJECT_PROTECTION" +
                " (OBJECT_PATH, CREATOR_IDENTITY_NAME, OWNER_ROLE_NAME, SECURITY_TYPE_NAME)" +
                " VALUES ('/freespacemgrs/NODE1:POOL1', 'SYSTEM', 'SYSADM', 'SHARED')"
        );
        executeUpdate(
            "INSERT INTO SEC_ACL_MAP (OBJECT_PATH, ROLE_NAME, ACCESS_TYPE)" +
                " VALUES ('/freespacemgrs/NODE1:POOL1', 'USER', 7)"
        );

        migrateTo(TEST_VERSION);

        // ':' is replaced by ';' in both FSM name columns
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_NAME", "POOL1"))).isEqualTo("NODE1;POOL1");
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_DSP_NAME", "POOL1"))).isEqualTo("node1;pool1");

        // FSM names without a colon stay untouched
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_NAME", "DFLTSTORPOOL"))).isEqualTo("NOCOLONFSM");
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_DSP_NAME", "DFLTSTORPOOL"))).isEqualTo("NoColonFsm");

        // all colons are replaced, not just the first one
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_NAME", "DFLTDISKLESSSTORPOOL"))).isEqualTo("A;B;C");
        assertThat(querySingleString(selectFsm("FREE_SPACE_MGR_DSP_NAME", "DFLTDISKLESSSTORPOOL")))
            .isEqualTo("a;b;c");

        // free space manager object protection and ACL entries are gone
        assertThat(countRows("SEC_OBJECT_PROTECTION WHERE OBJECT_PATH LIKE '%freespacemgrs%'")).isZero();
        assertThat(countRows("SEC_ACL_MAP WHERE OBJECT_PATH LIKE '%freespacemgrs%'")).isZero();

        // unrelated object protection entries and their ACLs are untouched
        assertThat(countRows("SEC_OBJECT_PROTECTION WHERE OBJECT_PATH = '/sys/controller/nodesMap'")).isEqualTo(1);
        assertThat(countRows("SEC_ACL_MAP WHERE OBJECT_PATH = '/sys/controller/nodesMap'")).isEqualTo(3);
    }

    private String selectFsm(String column, String poolName)
    {
        return "SELECT " + column + " FROM NODE_STOR_POOL WHERE NODE_NAME = 'NODE1' AND POOL_NAME = '" +
            poolName + "'";
    }
}
