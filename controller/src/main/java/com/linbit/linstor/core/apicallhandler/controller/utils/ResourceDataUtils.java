package com.linbit.linstor.core.apicallhandler.controller.utils;

import com.linbit.ImplementationError;
import com.linbit.linstor.core.apicallhandler.response.ApiDatabaseException;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.Resource.Flags;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.layer.resource.CtrlRscLayerDataFactory;
import com.linbit.linstor.stateflags.StateFlags;
import com.linbit.linstor.storage.interfaces.categories.resource.AbsRscLayerObject;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.utils.layer.LayerRscUtils;


import java.util.Set;

public class ResourceDataUtils
{
    private ResourceDataUtils()
    {
    }

    public static boolean recalculateVolatileRscData(
        CtrlRscLayerDataFactory ctrlRscLayerDataFactoryRef,
        Resource rscRef
    )
    {
        boolean changed = false;
        try
        {
            changed = ctrlRscLayerDataFactoryRef.recalculateVolatileRscData(rscRef);
        }
        catch (DatabaseException sqlExc)
        {
            throw new ApiDatabaseException(sqlExc);
        }
        return changed;
    }

    public enum DrbdResourceResult
    {
        DISKFUL, DISKLESS, TIE_BREAKER, NO_DRBD;
    }

    public static DrbdResourceResult isDrbdResource(Resource rsc)
    {
        return isDrbdResource(rsc, false, false);
    }

    public static DrbdResourceResult isDrbdResource(
        Resource rsc,
        boolean ignoreDeleteFlag,
        boolean ignoreInactiveFlag
    )
    {
        final DrbdResourceResult ret;

        StateFlags<Flags> rscFlags = rsc.getStateFlags();
        boolean deleteOrInactive = !ignoreDeleteFlag && rscFlags.isSet(Resource.Flags.DELETE);
        deleteOrInactive |= !ignoreInactiveFlag && rscFlags.isSomeSet(
            Resource.Flags.INACTIVE,
            Resource.Flags.INACTIVATING
        );

        if (deleteOrInactive)
        {
            ret = DrbdResourceResult.NO_DRBD;
        }
        else
        {
            final AbsRscLayerObject<Resource> rscData = rsc.getLayerData();
            final Set<AbsRscLayerObject<Resource>> drbdRscDataSet = LayerRscUtils.getRscDataByLayer(
                rscData,
                DeviceLayerKind.DRBD
            );
            if (drbdRscDataSet.size() > 1)
            {
                throw new ImplementationError("More DrbdRscData received than expected");
            }
            else if (drbdRscDataSet.isEmpty())
            {
                ret = DrbdResourceResult.NO_DRBD;
            }
            else
            {
                AbsRscLayerObject<Resource> drbdRscData = drbdRscDataSet.iterator().next();
                if (!drbdRscData.hasAnyPreventExecutionIgnoreReason())
                {
                    if (rscFlags.isSet(Resource.Flags.DRBD_DISKLESS))
                    {
                        ret = rscFlags.isSet(Resource.Flags.TIE_BREAKER) ?
                            DrbdResourceResult.TIE_BREAKER :
                            DrbdResourceResult.DISKLESS;
                    }
                    else
                    {
                        ret = DrbdResourceResult.DISKFUL;
                    }
                }
                else
                {
                    ret = DrbdResourceResult.NO_DRBD;
                }
            }
        }
        return ret;
    }
}
