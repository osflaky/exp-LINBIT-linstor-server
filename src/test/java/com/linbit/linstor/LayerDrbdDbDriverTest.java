package com.linbit.linstor;

import com.linbit.linstor.core.identifier.VolumeNumber;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.ResourceDefinition;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.core.types.TcpPortNumber;
import com.linbit.linstor.security.GenericDbBase;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdRscData;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdRscDfnData;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdVlmData;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdVlmDfnData;
import com.linbit.linstor.storage.interfaces.layers.drbd.DrbdRscDfnObject.TransportType;
import com.linbit.linstor.storage.interfaces.layers.drbd.DrbdRscObject.DrbdRscFlags;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LayerDrbdDbDriverTest extends GenericDbBase
{
    private static final String LAYER_RESOURCE_ID = "LAYER_RESOURCE_ID";
    private static final String PEER_SLOTS = "PEER_SLOTS";
    private static final String AL_STRIPES = "AL_STRIPES";
    private static final String AL_STRIPE_SIZE = "AL_STRIPE_SIZE";
    private static final String FLAGS = "FLAGS";
    private static final String TCP_PORT_LIST = "TCP_PORT_LIST";
    private static final String RESOURCE_NAME_SUFFIX = "RESOURCE_NAME_SUFFIX";
    private static final String SNAPSHOT_NAME = "SNAPSHOT_NAME";

    private static final String SELECT_ALL_DRBD_RESOURCES =
        " SELECT " + LAYER_RESOURCE_ID + ", " + PEER_SLOTS + ", " + AL_STRIPES + ", " + AL_STRIPE_SIZE + ", " +
                     FLAGS + ", " + NODE_ID + ", " + TCP_PORT_LIST +
        " FROM " + TBL_LAYER_DRBD_RESOURCES;

    private static final String SELECT_ALL_DRBD_RSC_DFNS =
        " SELECT " + RESOURCE_NAME + ", " + RESOURCE_NAME_SUFFIX + ", " + SNAPSHOT_NAME + ", " + PEER_SLOTS + ", " +
                     AL_STRIPES + ", " + AL_STRIPE_SIZE + ", " + TCP_PORT + ", " + TRANSPORT_TYPE + ", " + SECRET +
        " FROM " + TBL_LAYER_DRBD_RESOURCE_DEFINITIONS;

    private static final String SELECT_ALL_DRBD_VOLUMES =
        " SELECT " + LAYER_RESOURCE_ID + ", " + VLM_NR + ", " + NODE_NAME + ", " + POOL_NAME +
        " FROM " + TBL_LAYER_DRBD_VOLUMES;

    private static final String SELECT_ALL_DRBD_VLM_DFNS =
        " SELECT " + RESOURCE_NAME + ", " + RESOURCE_NAME_SUFFIX + ", " + SNAPSHOT_NAME + ", " + VLM_NR + ", " +
                     VLM_MINOR_NR +
        " FROM " + TBL_LAYER_DRBD_VOLUME_DEFINITIONS;

    private static final String NODE_NAME_STR = "node";
    private static final String RSC_NAME_STR = "rsc";
    private static final String SP_NAME_STR = "mySp";
    private static final int VLM_NR_INT = 0;
    private static final long VLM_SIZE = 10 * 1024L;

    private Resource rsc;
    private DrbdRscData<Resource> drbdRscData;
    private DrbdRscDfnData<Resource> drbdRscDfnData;
    private DrbdVlmData<Resource> drbdVlmData;
    private DrbdVlmDfnData<Resource> drbdVlmDfnData;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        super.setUpAndEnterScope();

        rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Arrays.asList(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE))
            .build();
        StorPool storPool = storPoolTestFactory.builder(NODE_NAME_STR, SP_NAME_STR)
            .setDriverKind(DeviceProviderKind.LVM)
            .build();
        volumeTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR, VLM_NR_INT)
            .setSize(VLM_SIZE)
            .setStorPoolData(storPool)
            .build();

        drbdRscData = (DrbdRscData<Resource>) rsc.getLayerData();
        drbdRscDfnData = drbdRscData.getRscDfnLayerObject();
        drbdVlmData = drbdRscData.getVlmLayerObjects().get(new VolumeNumber(VLM_NR_INT));
        drbdVlmDfnData = drbdVlmData.getVlmDfnLayerObject();

        commit();
    }

    @Test
    public void testPersistedDrbdRsc() throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_RESOURCES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(drbdRscData.getRscLayerId(), resultSet.getInt(LAYER_RESOURCE_ID));
            assertEquals(drbdRscData.getPeerSlots(), resultSet.getInt(PEER_SLOTS));
            assertEquals(drbdRscData.getAlStripes(), resultSet.getInt(AL_STRIPES));
            assertEquals(drbdRscData.getAlStripeSize(), resultSet.getLong(AL_STRIPE_SIZE));
            assertEquals(drbdRscData.getFlags().getFlagsBits(), resultSet.getLong(FLAGS));
            assertEquals(drbdRscData.getNodeId().value, resultSet.getInt(NODE_ID));
            assertEquals(expectedTcpPortList(), resultSet.getString(TCP_PORT_LIST));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testPersistedDrbdRscDfn() throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_RSC_DFNS);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(RSC_NAME_STR.toUpperCase(), resultSet.getString(RESOURCE_NAME));
            assertEquals("", resultSet.getString(RESOURCE_NAME_SUFFIX));
            assertEquals("", resultSet.getString(SNAPSHOT_NAME));
            assertEquals(drbdRscDfnData.getPeerSlots(), resultSet.getInt(PEER_SLOTS));
            assertEquals(drbdRscDfnData.getAlStripes(), resultSet.getInt(AL_STRIPES));
            assertEquals(drbdRscDfnData.getAlStripeSize(), resultSet.getLong(AL_STRIPE_SIZE));
            TcpPortNumber tcpPort = drbdRscDfnData.getTcpPort();
            if (tcpPort == null)
            {
                assertNull(resultSet.getObject(TCP_PORT));
            }
            else
            {
                assertEquals(Integer.valueOf(tcpPort.value), resultSet.getObject(TCP_PORT));
            }
            assertEquals(TransportType.IP.name(), resultSet.getString(TRANSPORT_TYPE));
            assertEquals(drbdRscDfnData.getSecret(), resultSet.getString(SECRET));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testPersistedDrbdVlm() throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_VOLUMES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(drbdRscData.getRscLayerId(), resultSet.getInt(LAYER_RESOURCE_ID));
            assertEquals(VLM_NR_INT, resultSet.getInt(VLM_NR));
            // internal DRBD meta data -> no external meta data stor pool
            assertNull(resultSet.getString(NODE_NAME));
            assertNull(resultSet.getString(POOL_NAME));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testPersistedDrbdVlmDfn() throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_VLM_DFNS);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(RSC_NAME_STR.toUpperCase(), resultSet.getString(RESOURCE_NAME));
            assertEquals("", resultSet.getString(RESOURCE_NAME_SUFFIX));
            assertEquals("", resultSet.getString(SNAPSHOT_NAME));
            assertEquals(VLM_NR_INT, resultSet.getInt(VLM_NR));
            assertEquals(drbdVlmDfnData.getMinorNr().value, resultSet.getInt(VLM_MINOR_NR));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testUpdateRscFlags() throws Exception
    {
        assertFalse(drbdRscData.getFlags().isSet(DrbdRscFlags.DELETE));

        drbdRscData.getFlags().enableFlags(DrbdRscFlags.DELETE);
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_RESOURCES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            long persistedFlags = resultSet.getLong(FLAGS);
            assertEquals(drbdRscData.getFlags().getFlagsBits(), persistedFlags);
            assertNotEquals(0, persistedFlags & DrbdRscFlags.DELETE.flagValue);
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testUpdateRscDfnSecret() throws Exception
    {
        String newSecret = "changedSecret";
        assertNotEquals(newSecret, drbdRscDfnData.getSecret());

        drbdRscDfnData.setSecret(newSecret);
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_RSC_DFNS);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(newSecret, resultSet.getString(SECRET));
            assertFalse(resultSet.next());
        }
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testUpdateRscDfnTcpPort() throws Exception
    {
        drbdRscDfnData.setPort(9179);
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_DRBD_RSC_DFNS);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(Integer.valueOf(9179), resultSet.getObject(TCP_PORT));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testDelete() throws Exception
    {
        assertEquals(1, countRows(TBL_LAYER_DRBD_RESOURCES));
        assertEquals(1, countRows(TBL_LAYER_DRBD_VOLUMES));

        ResourceDefinition rscDfn = rsc.getResourceDefinition();
        rsc.delete();
        commit();

        assertEquals(0, countRows(TBL_LAYER_DRBD_RESOURCES));
        assertEquals(0, countRows(TBL_LAYER_DRBD_VOLUMES));
        assertEquals(0, countRows(TBL_LAYER_RESOURCE_IDS));

        // definition-level data is only deleted with the resource definition
        assertEquals(1, countRows(TBL_LAYER_DRBD_RESOURCE_DEFINITIONS));
        assertEquals(1, countRows(TBL_LAYER_DRBD_VOLUME_DEFINITIONS));

        rscDfn.delete();
        commit();

        assertEquals(0, countRows(TBL_LAYER_DRBD_RESOURCE_DEFINITIONS));
        assertEquals(0, countRows(TBL_LAYER_DRBD_VOLUME_DEFINITIONS));
    }

    private String expectedTcpPortList()
    {
        StringBuilder sb = new StringBuilder("[");
        for (TcpPortNumber port : drbdRscData.getTcpPortList())
        {
            sb.append(port.value).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private int countRows(String table) throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT COUNT(*) FROM " + table);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }
}
