package com.linbit.linstor;

import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.security.GenericDbBase;
import com.linbit.linstor.storage.interfaces.categories.resource.AbsRscLayerObject;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LayerResourceIdDbDriverTest extends GenericDbBase
{
    private static final String LAYER_RESOURCE_ID = "LAYER_RESOURCE_ID";
    private static final String LAYER_RESOURCE_KIND = "LAYER_RESOURCE_KIND";
    private static final String LAYER_RESOURCE_PARENT_ID = "LAYER_RESOURCE_PARENT_ID";
    private static final String LAYER_RESOURCE_SUFFIX = "LAYER_RESOURCE_SUFFIX";
    private static final String LAYER_RESOURCE_SUSPENDED = "LAYER_RESOURCE_SUSPENDED";
    private static final String SNAPSHOT_NAME = "SNAPSHOT_NAME";

    private static final String SELECT_ALL_LAYER_RSC_IDS =
        " SELECT " + LAYER_RESOURCE_ID + ", " + NODE_NAME + ", " + RESOURCE_NAME + ", " + SNAPSHOT_NAME + ", " +
                     LAYER_RESOURCE_KIND + ", " + LAYER_RESOURCE_PARENT_ID + ", " + LAYER_RESOURCE_SUFFIX + ", " +
                     LAYER_RESOURCE_SUSPENDED +
        " FROM " + TBL_LAYER_RESOURCE_IDS +
        " ORDER BY " + LAYER_RESOURCE_ID;

    private static final String NODE_NAME_STR = "node";
    private static final String RSC_NAME_STR = "rsc";

    @Before
    public void setUp() throws Exception
    {
        super.setUpAndEnterScope();
    }

    @Test
    public void testPersistDrbdStorageStack() throws Exception
    {
        Resource rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Arrays.asList(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE))
            .build();
        commit();

        AbsRscLayerObject<Resource> drbdRscData = rsc.getLayerData();
        AbsRscLayerObject<Resource> storRscData = drbdRscData.getChildren().iterator().next();

        Map<Integer, Row> rows = loadAllRows();
        assertEquals(2, rows.size());

        Row drbdRow = rows.get(drbdRscData.getRscLayerId());
        assertEquals(DeviceLayerKind.DRBD.name(), drbdRow.kind);
        assertNull(drbdRow.parentId);
        assertCommonColumns(drbdRow);

        Row storRow = rows.get(storRscData.getRscLayerId());
        assertEquals(DeviceLayerKind.STORAGE.name(), storRow.kind);
        assertEquals(Integer.valueOf(drbdRscData.getRscLayerId()), storRow.parentId);
        assertCommonColumns(storRow);
    }

    @Test
    public void testPersistStorageOnlyStack() throws Exception
    {
        Resource rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Collections.singletonList(DeviceLayerKind.STORAGE))
            .build();
        commit();

        AbsRscLayerObject<Resource> storRscData = rsc.getLayerData();

        Map<Integer, Row> rows = loadAllRows();
        assertEquals(1, rows.size());

        Row storRow = rows.get(storRscData.getRscLayerId());
        assertEquals(DeviceLayerKind.STORAGE.name(), storRow.kind);
        assertNull(storRow.parentId);
        assertCommonColumns(storRow);
    }

    @Test
    public void testPersistNvmeStorageStack() throws Exception
    {
        Resource rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Arrays.asList(DeviceLayerKind.NVME, DeviceLayerKind.STORAGE))
            .build();
        commit();

        AbsRscLayerObject<Resource> nvmeRscData = rsc.getLayerData();
        AbsRscLayerObject<Resource> storRscData = nvmeRscData.getChildren().iterator().next();

        Map<Integer, Row> rows = loadAllRows();
        assertEquals(2, rows.size());

        Row nvmeRow = rows.get(nvmeRscData.getRscLayerId());
        assertEquals(DeviceLayerKind.NVME.name(), nvmeRow.kind);
        assertNull(nvmeRow.parentId);
        assertCommonColumns(nvmeRow);

        Row storRow = rows.get(storRscData.getRscLayerId());
        assertEquals(DeviceLayerKind.STORAGE.name(), storRow.kind);
        assertEquals(Integer.valueOf(nvmeRscData.getRscLayerId()), storRow.parentId);
        assertCommonColumns(storRow);
    }

    @Test
    public void testUpdateSuspended() throws Exception
    {
        Resource rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Arrays.asList(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE))
            .build();
        commit();

        AbsRscLayerObject<Resource> drbdRscData = rsc.getLayerData();
        AbsRscLayerObject<Resource> storRscData = drbdRscData.getChildren().iterator().next();

        drbdRscData.setShouldSuspendIo(true);
        commit();

        Map<Integer, Row> rows = loadAllRows();
        assertEquals(2, rows.size());
        assertTrue(rows.get(drbdRscData.getRscLayerId()).suspended);
        assertFalse(rows.get(storRscData.getRscLayerId()).suspended);
    }

    @Test
    public void testDelete() throws Exception
    {
        Resource rsc = resourceTestFactory.builder(NODE_NAME_STR, RSC_NAME_STR)
            .setLayerStack(Arrays.asList(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE))
            .build();
        commit();

        assertEquals(2, loadAllRows().size());

        rsc.delete();
        commit();

        assertEquals(0, loadAllRows().size());
    }

    private void assertCommonColumns(Row row)
    {
        assertEquals(NODE_NAME_STR.toUpperCase(), row.nodeName);
        assertEquals(RSC_NAME_STR.toUpperCase(), row.rscName);
        assertEquals("", row.snapName);
        assertEquals("", row.suffix);
        assertFalse(row.suspended);
    }

    private Map<Integer, Row> loadAllRows() throws Exception
    {
        Map<Integer, Row> rows = new HashMap<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_LAYER_RSC_IDS);
            ResultSet resultSet = stmt.executeQuery())
        {
            while (resultSet.next())
            {
                Row row = new Row();
                row.layerRscId = resultSet.getInt(LAYER_RESOURCE_ID);
                row.nodeName = resultSet.getString(NODE_NAME);
                row.rscName = resultSet.getString(RESOURCE_NAME);
                row.snapName = resultSet.getString(SNAPSHOT_NAME);
                row.kind = resultSet.getString(LAYER_RESOURCE_KIND);
                row.parentId = (Integer) resultSet.getObject(LAYER_RESOURCE_PARENT_ID);
                row.suffix = resultSet.getString(LAYER_RESOURCE_SUFFIX);
                row.suspended = resultSet.getBoolean(LAYER_RESOURCE_SUSPENDED);
                rows.put(row.layerRscId, row);
            }
        }
        return rows;
    }

    private static class Row
    {
        private int layerRscId;
        private String nodeName;
        private String rscName;
        private String snapName;
        private String kind;
        private Integer parentId;
        private String suffix;
        private boolean suspended;
    }
}
