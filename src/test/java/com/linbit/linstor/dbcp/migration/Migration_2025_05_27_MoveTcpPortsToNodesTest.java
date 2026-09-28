package com.linbit.linstor.dbcp.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:typename")
public class Migration_2025_05_27_MoveTcpPortsToNodesTest extends MigrationTestBase
{
    private static final String PRE_VERSION = "2025.03.18.08.00";
    private static final String TEST_VERSION = "2025.05.27.08.00";

    @Test
    public void moveTcpPortsToNodes() throws Exception
    {
        startDbAtVersion("moveTcpPortsToNodes", PRE_VERSION);
        insertLegacyData();

        migrateTo(TEST_VERSION);

        // resources of RSC1 get the port of their DRBD resource definition
        assertThat(querySingleString(selectPortList(1))).isEqualTo("[7000]");
        assertThat(querySingleString(selectPortList(2))).isEqualTo("[7000]");
        // DRBD snapshot data gets the placeholder port list
        assertThat(querySingleString(selectPortList(3))).isEqualTo("[-1]");
        // resource without a port on its DRBD resource definition keeps the column default
        assertThat(querySingleString(selectPortList(4))).isEqualTo("[]");

        // TCP_PORT of RESOURCE_CONNECTIONS was renamed to TCP_PORT_SRC ...
        assertThat(
            countRows(
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'LINSTOR'" +
                    " AND TABLE_NAME = 'RESOURCE_CONNECTIONS' AND COLUMN_NAME = 'TCP_PORT'"
            )
        ).isZero();
        // ... and its value was copied to the new TCP_PORT_DST column
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_SRC", "RSC1", ""))).isEqualTo(7100);
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_DST", "RSC1", ""))).isEqualTo(7100);

        // resource connections without a port keep NULL in both columns
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_SRC", "RSC2", ""))).isNull();
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_DST", "RSC2", ""))).isNull();

        // snapshot resource connections keep their (renamed) port, but do not get a TCP_PORT_DST
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_SRC", "RSC1", "SNAP1"))).isEqualTo(7200);
        assertThat(querySingleInt(selectRscConPort("TCP_PORT_DST", "RSC1", "SNAP1"))).isNull();

        // the (not yet dropped) TCP_PORT column of the DRBD resource definitions is untouched
        assertThat(
            querySingleInt(
                "SELECT TCP_PORT FROM LAYER_DRBD_RESOURCE_DEFINITIONS" +
                    " WHERE RESOURCE_NAME = 'RSC1' AND SNAPSHOT_NAME = ''"
            )
        ).isEqualTo(7000);
    }

    private void insertLegacyData() throws Exception
    {
        executeUpdate(
            "INSERT INTO NODES (UUID, NODE_NAME, NODE_DSP_NAME, NODE_FLAGS, NODE_TYPE) VALUES" +
                " ('10000000-0000-0000-0000-000000000001', 'N1', 'n1', 0, 2)," +
                " ('10000000-0000-0000-0000-000000000002', 'N2', 'n2', 0, 2)"
        );
        executeUpdate(
            "INSERT INTO RESOURCE_DEFINITIONS (UUID, RESOURCE_NAME, SNAPSHOT_NAME, RESOURCE_DSP_NAME," +
                " SNAPSHOT_DSP_NAME, RESOURCE_FLAGS, LAYER_STACK) VALUES" +
                " ('20000000-0000-0000-0000-000000000001', 'RSC1', '', 'rsc1', '', 0, '[\"DRBD\",\"STORAGE\"]')," +
                " ('20000000-0000-0000-0000-000000000002', 'RSC1', 'SNAP1', 'rsc1', 'snap1', 0," +
                "  '[\"DRBD\",\"STORAGE\"]')," +
                " ('20000000-0000-0000-0000-000000000003', 'RSC2', '', 'rsc2', '', 0, '[\"DRBD\",\"STORAGE\"]')"
        );
        executeUpdate(
            "INSERT INTO RESOURCES (UUID, NODE_NAME, RESOURCE_NAME, SNAPSHOT_NAME, RESOURCE_FLAGS) VALUES" +
                " ('30000000-0000-0000-0000-000000000001', 'N1', 'RSC1', '', 0)," +
                " ('30000000-0000-0000-0000-000000000002', 'N2', 'RSC1', '', 0)," +
                " ('30000000-0000-0000-0000-000000000003', 'N1', 'RSC1', 'SNAP1', 0)," +
                " ('30000000-0000-0000-0000-000000000004', 'N2', 'RSC1', 'SNAP1', 0)," +
                " ('30000000-0000-0000-0000-000000000005', 'N1', 'RSC2', '', 0)," +
                " ('30000000-0000-0000-0000-000000000006', 'N2', 'RSC2', '', 0)"
        );
        // RSC1 has a TCP port on its DRBD resource definition, RSC2 and the snapshot do not
        executeUpdate(
            "INSERT INTO LAYER_DRBD_RESOURCE_DEFINITIONS (RESOURCE_NAME, RESOURCE_NAME_SUFFIX, SNAPSHOT_NAME," +
                " PEER_SLOTS, AL_STRIPES, AL_STRIPE_SIZE, TCP_PORT, TRANSPORT_TYPE, SECRET) VALUES" +
                " ('RSC1', '', '', 7, 1, 32, 7000, 'IP', 'secret1')," +
                " ('RSC1', '', 'SNAP1', 7, 1, 32, NULL, 'IP', NULL)," +
                " ('RSC2', '', '', 7, 1, 32, NULL, 'IP', 'secret2')"
        );
        executeUpdate(
            "INSERT INTO LAYER_RESOURCE_IDS (LAYER_RESOURCE_ID, NODE_NAME, RESOURCE_NAME, SNAPSHOT_NAME," +
                " LAYER_RESOURCE_KIND, LAYER_RESOURCE_SUFFIX) VALUES" +
                " (1, 'N1', 'RSC1', '', 'DRBD', '')," +
                " (2, 'N2', 'RSC1', '', 'DRBD', '')," +
                " (3, 'N1', 'RSC1', 'SNAP1', 'DRBD', '')," +
                " (4, 'N1', 'RSC2', '', 'DRBD', '')," +
                " (5, 'N2', 'RSC2', '', 'STORAGE', '')"
        );
        executeUpdate(
            "INSERT INTO LAYER_DRBD_RESOURCES (LAYER_RESOURCE_ID, PEER_SLOTS, AL_STRIPES, AL_STRIPE_SIZE," +
                " FLAGS, NODE_ID) VALUES" +
                " (1, 7, 1, 32, 0, 0)," +
                " (2, 7, 1, 32, 0, 1)," +
                " (3, 7, 1, 32, 0, 0)," +
                " (4, 7, 1, 32, 0, 0)"
        );
        // one connection with a port, one without and one belonging to a snapshot
        executeUpdate(
            "INSERT INTO RESOURCE_CONNECTIONS (UUID, NODE_NAME_SRC, NODE_NAME_DST, RESOURCE_NAME," +
                " SNAPSHOT_NAME, FLAGS, TCP_PORT) VALUES" +
                " ('40000000-0000-0000-0000-000000000001', 'N1', 'N2', 'RSC1', '', 0, 7100)," +
                " ('40000000-0000-0000-0000-000000000002', 'N1', 'N2', 'RSC2', '', 0, NULL)," +
                " ('40000000-0000-0000-0000-000000000003', 'N1', 'N2', 'RSC1', 'SNAP1', 0, 7200)"
        );
    }

    private String selectPortList(int layerRscId)
    {
        return "SELECT TCP_PORT_LIST FROM LAYER_DRBD_RESOURCES WHERE LAYER_RESOURCE_ID = " + layerRscId;
    }

    private String selectRscConPort(String column, String rscName, String snapName)
    {
        return "SELECT " + column + " FROM RESOURCE_CONNECTIONS WHERE NODE_NAME_SRC = 'N1'" +
            " AND NODE_NAME_DST = 'N2' AND RESOURCE_NAME = '" + rscName + "' AND SNAPSHOT_NAME = '" +
            snapName + "'";
    }
}
