package com.linbit.linstor.core.utils;

import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.ResourceDefinition;
import com.linbit.linstor.storage.interfaces.categories.resource.AbsRscLayerObject;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.utils.layer.LayerRscUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class ResourceUtils
{
    public static HashSet<Resource> filterResourcesDiskless(ResourceDefinition rscDfn)
    {
        return filterResources(rscDfn, rsc -> rsc.getStateFlags().isSet(Resource.Flags.DISKLESS));
    }

    public static HashSet<Resource> filterResourcesDrbdDiskless(ResourceDefinition rscDfn)
    {
        return filterResources(rscDfn, rsc -> rsc.getStateFlags().isSet(Resource.Flags.DRBD_DISKLESS));
    }

    public static HashSet<Resource> filterResourcesDrbdDiskfulActive(ResourceDefinition rscDfn)
    {
        return filterResources(rscDfn, rsc ->
        {
            boolean match;
            Set<AbsRscLayerObject<Resource>> drbdRscDataSet = LayerRscUtils.getRscDataByLayer(
                rsc.getLayerData(),
                DeviceLayerKind.DRBD
            );
            match = !drbdRscDataSet.isEmpty();
            if (match)
            {
                for (AbsRscLayerObject<Resource> rscData : drbdRscDataSet)
                {
                    if (rscData.hasAnyPreventExecutionIgnoreReason())
                    {
                        match = false;
                        break;
                    }
                }
            }
            return match;
        });
    }

    public static HashSet<Resource> filterResources(
        ResourceDefinition rscDfn,
        Predicate<Resource> filter
    )
    {
        HashSet<Resource> ret = new HashSet<>();

        Iterator<Resource> rscIt = rscDfn.iterateResource();
        while (rscIt.hasNext())
        {
            Resource rsc = rscIt.next();
            if (filter.test(rsc))
            {
                ret.add(rsc);
            }
        }

        return ret;
    }
}
