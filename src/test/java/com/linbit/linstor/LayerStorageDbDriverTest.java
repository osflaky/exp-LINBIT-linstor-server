package com.linbit.linstor;

import com.linbit.linstor.core.identifier.VolumeNumber;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.security.GenericDbBase;
import com.linbit.linstor.storage.data.provider.StorageRscData;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LayerStorageDbDriverTest extends GenericDbBase
{
    private static final String LAYER_RESOURCE_ID = "LAYER_RESOURCE_ID";
    private static final String PROVIDER_KIND = "PROVIDER_KIND";

    private static final String SELECT_ALL_STORAGE_VOLUMES =
        " SELECT " + LAYER_RESOURCE_ID + ", " + VLM_NR + ", " + PROVIDER_KIND + ", " + NODE_NAME + ", " +
                     STOR_POOL_NAME +
        " FROM " + TBL_LAYER_STORAGE_VOLUMES +
        " ORDER BY " + VLM_NR;

    private static final String NODE_NAME_STR = "node";
    private static final String RSC_NAME_STR = "rsc";
    private static final String SP_NAME_STR = "mySp";
    private static final int VLM_NR_INT = 0;
    private static final long VLM_SIZE = 10 * 1024L;

    private Resource rsc;
    private StorPool storPool;
    private StorageRscData<Resource> storRscData;
    private VlmProviderObject<Resource> storVlmData;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        super.setUpAndEnterScope();

        rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Collections.singletonList(DeviceLayerKind.STORAGE))
            .build();
        storPool = storPoolTestFactory.builder(NODE_NAME_STR, SP_NAME_STR)
            .setDriverKind(DeviceProviderKind.LVM)
            .build();
        volumeTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR, VLM_NR_INT)
            .setSize(VLM_SIZE)
            .setStorPoolData(storPool)
            .build();

        storRscData = (StorageRscData<Resource>) rsc.getLayerData();
        storVlmData = storRscData.getVlmProviderObject(new VolumeNumber(VLM_NR_INT));

        commit();
    }

    @Test
    public void testPersistedStorageVlm() throws Exception
    {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_STORAGE_VOLUMES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(storRscData.getRscLayerId(), resultSet.getInt(LAYER_RESOURCE_ID));
            assertEquals(VLM_NR_INT, resultSet.getInt(VLM_NR));
            assertEquals(DeviceProviderKind.LVM.name(), resultSet.getString(PROVIDER_KIND));
            assertEquals(NODE_NAME_STR.toUpperCase(), resultSet.getString(NODE_NAME));
            assertEquals(SP_NAME_STR.toUpperCase(), resultSet.getString(STOR_POOL_NAME));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testPersistSecondVolume() throws Exception
    {
        int secondVlmNr = 1;
        volumeTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR, secondVlmNr)
            .setSize(VLM_SIZE)
            .setStorPoolData(storPool)
            .build();
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_STORAGE_VOLUMES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(VLM_NR_INT, resultSet.getInt(VLM_NR));
            assertTrue(resultSet.next());
            assertEquals(secondVlmNr, resultSet.getInt(VLM_NR));
            assertEquals(storRscData.getRscLayerId(), resultSet.getInt(LAYER_RESOURCE_ID));
            assertEquals(DeviceProviderKind.LVM.name(), resultSet.getString(PROVIDER_KIND));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testUpdateStorPool() throws Exception
    {
        String otherSpName = "otherSp";
        StorPool otherStorPool = storPoolTestFactory.builder(NODE_NAME_STR, otherSpName)
            .setDriverKind(DeviceProviderKind.LVM)
            .build();

        storVlmData.setStorPool(otherStorPool);
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_STORAGE_VOLUMES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertTrue(resultSet.next());
            assertEquals(NODE_NAME_STR.toUpperCase(), resultSet.getString(NODE_NAME));
            assertEquals(otherSpName.toUpperCase(), resultSet.getString(STOR_POOL_NAME));
            assertFalse(resultSet.next());
        }
    }

    @Test
    public void testDelete() throws Exception
    {
        rsc.delete();
        commit();

        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_STORAGE_VOLUMES);
            ResultSet resultSet = stmt.executeQuery())
        {
            assertFalse(resultSet.next());
        }
    }
}
