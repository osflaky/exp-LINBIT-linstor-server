package com.linbit.linstor.core.objects.db.utils;

import com.linbit.linstor.core.objects.AbsResource;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.Snapshot;
import com.linbit.linstor.dbdrivers.DatabaseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class K8sCrdUtils
{
    /**
     * Returns a checked volume map for the given layer resource, ensuring consistency with the database.
     *
     * @param <T> Layer specific Volume-type (LayerDrbdVolumesSpec, LayerBcacheVolumesSpec, ...)
     * @param <RSC> Subtype of AbsResource (currently only Resource or Snapshot)
     * @param absRsc Given resource or snapshot (used to determine expected volume entries
     * @param cachedMap The map that is queried of the layerResourceId
     *
     * @return a not null map. If the cacheMap returns a null, an additional check is performed
     * if there are volume-definitions or snapshot-definitions. If there are an exception is thrown as we are missing
     * entries from the database.
     * Otherwise an empty map is returned so the caller can safely iterate over the map in any case
     *
     */
    public static <T, RSC extends AbsResource<RSC>> Map<Integer, T> getCheckedVlmMap(
        RSC absRsc,
        HashMap<Integer, HashMap<Integer, T>> cachedMap,
        int layerResourceId
    )
        throws DatabaseException
    {
        Map<Integer, T> vlmMap = cachedMap.get(layerResourceId);

        if (vlmMap == null)
        {
            int expectedVlmCount;
            String expectedType;
            if (absRsc instanceof Resource)
            {
                expectedVlmCount = absRsc.getResourceDefinition().getVolumeDfnCount();
                expectedType = "volume-definitions";
            }
            else
            {
                expectedVlmCount = ((Snapshot) absRsc).getSnapshotDefinition()
                    .getAllSnapshotVolumeDefinitions().size();
                expectedType = "snapshot-volume-definitions";
            }

            if (expectedVlmCount > 0)
            {
                throw new DatabaseException(
                    "No volume entries found for layerid: " + layerResourceId + " although " +
                        expectedVlmCount + " " + expectedType + " exist"
                );
            }
            else
            {
                // just to prevent NPE when trying to iterate over the returned map
                vlmMap = Collections.emptyMap();
            }
        }
        return vlmMap;
    }
}
