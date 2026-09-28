package com.linbit.linstor.core.apicallhandler.controller.utils;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.ResourceDefinition;
import com.linbit.linstor.core.objects.Snapshot;
import com.linbit.linstor.core.objects.SnapshotDefinition;
import com.linbit.linstor.core.objects.SnapshotVolume;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.core.objects.VolumeDefinition;
import com.linbit.linstor.propscon.ReadOnlyProps;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;
import com.linbit.linstor.utils.layer.LayerRscUtils;
import com.linbit.linstor.utils.layer.LayerVlmUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Checks that need to pass before a volume definition may be resized.
 */
public class VolumeDefinitionResizeCheckUtils
{
    private VolumeDefinitionResizeCheckUtils()
    {
    }

    /**
     * All participating layers & providers (including providers for metadata) must support shrinking, or an Exception
     * is thrown
     */
    public static void ensureShrinkingIsSupported(VolumeDefinition vlmDfnRef)
    {
        Iterator<Resource> rscIt = vlmDfnRef.getResourceDefinition().iterateResource();
        Set<DeviceLayerKind> layerKindSet = new HashSet<>();
        Set<DeviceProviderKind> providerKindSet = new HashSet<>();
        while (rscIt.hasNext())
        {
            Resource rsc = rscIt.next();
            layerKindSet.addAll(LayerRscUtils.getLayerStack(rsc));
            Set<StorPool> storPools = LayerVlmUtils.getStorPools(rsc, true);
            for (StorPool sp : storPools)
            {
                providerKindSet.add(sp.getDeviceProviderKind());
            }
        }

        for (DeviceLayerKind kind : layerKindSet)
        {
            if (!kind.isShrinkingSupported())
            {
                throw new ApiRcException(
                    ApiCallRcImpl.entryBuilder(
                        ApiConsts.FAIL_INVLD_VLM_SIZE,
                        "Shrinking volumes is not supported by layer '" + kind.name() + "'. " +
                            "Volumes can only grow in size."
                    )
                        .setSkipErrorReport(true)
                        .build()
                );
            }
        }
        for (DeviceProviderKind kind : providerKindSet)
        {
            if (!kind.isShrinkingSupported())
            {
                throw new ApiRcException(
                    ApiCallRcImpl.entryBuilder(
                        ApiConsts.FAIL_INVLD_VLM_SIZE,
                        "Shrinking volumes is not supported by storage provider '" + kind.name() + "'. " +
                            "Volumes can only grow in size."
                    )
                        .setSkipErrorReport(true)
                        .build()
                );
            }
        }
    }

    /**
     * All participating storage pools must have at least the additional free space left.
     * Thin pools always fulfill this requirement.
     *
     * NOTE:
     * The controller can only use the estimated additional size for checking. That means, even
     * if the controller (barely) passes this check, the satellite might still run into an issue
     * when executing the resize with the actual additional space, which might be more than we get
     * here.
     */
    public static void ensureAllStorPoolsHaveEnoughFreeSpace(VolumeDefinition vlmDfnRef, long additionalSize)
    {
        Iterator<Resource> rscIt = vlmDfnRef.getResourceDefinition().iterateResource();
        Set<StorPool.Key> storPoolKeySet = new TreeSet<>();
        while (rscIt.hasNext())
        {
            Resource rsc = rscIt.next();
            Set<StorPool> storPools = LayerVlmUtils.getStorPools(rsc, true);
            for (StorPool sp : storPools)
            {
                if (!sp.getDeviceProviderKind().usesThinProvisioning())
                {
                    if (sp.getFreeSpaceTracker().getFreeCapacityLastUpdated().orElse(0L) < additionalSize)
                    {
                        storPoolKeySet.add(new StorPool.Key(sp));
                    }
                }
            }
        }

        if (!storPoolKeySet.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            for (StorPool.Key key : storPoolKeySet)
            {
                sb.append("Node: ").append(key.getNodeName().displayValue)
                    .append(", StorPool: ").append(key.getStorPoolName().displayValue).append("\n");
            }
            sb.setLength(sb.length() - 1); // cut last \n

            throw new ApiRcException(
                ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_NOT_ENOUGH_FREE_SPACE,
                    "Cannot grow the volume definition by " + additionalSize +
                        "KiB, as the following storage pool do not have enough free space:\n" + sb
                )
            );
        }
    }

    /**
     * Throws an ApiRcException if the given vlmDfn's rscDfn has the "DrbdOption/ExactSize" property set to true
     *
     */
    public static void ensureExactSizeIsUnset(VolumeDefinition vlmDfnRef)
    {
        ReadOnlyProps rscDfnProps = vlmDfnRef.getResourceDefinition().getProps();
        @Nullable String exactSize = rscDfnProps.getProp(
            ApiConsts.KEY_DRBD_EXACT_SIZE,
            ApiConsts.NAMESPC_DRBD_OPTIONS
        );
        if (exactSize != null && Boolean.parseBoolean(exactSize))
        {
            throw new ApiRcException(
                ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_INVLD_PROP,
                    "Volume definition must not be resized while the resource-definition has the property '' set!",
                    true
                )
            );
        }
    }

    /**
     * Ensures that no shared storage pool backing the given volume definition has its resource active
     * on more than one node, as is the case during the dual-active window of a live migration.
     * The data shared by those resources cannot be safely resized while multiple nodes are accessing
     * it - refuse with a clear error instead of resizing the volume underneath the sharing nodes.
     */
    public static void ensureSharedDataNotActiveOnMultipleNodes(VolumeDefinition vlmDfnRef)
    {
        ResourceDefinition rscDfn = vlmDfnRef.getResourceDefinition();
        ResourceDefinitionUtils.ensureSharedDataNotActiveOnMultipleNodes(
            rscDfn,
            "Volume definition " + vlmDfnRef.getVolumeNumber() + " of '" + rscDfn.getName() + "'",
            "resized"
        );
    }

    /**
     * Ensures that the given volume definition has no thick LVM ({@link DeviceProviderKind#LVM}) snapshots.
     * <p>LVM does not allow resizing an active volume that has thick snapshots ("Snapshot origin volumes can be
     * resized only while inactive"). Refuse the resize with a clear error instead of letting "lvresize" fail
     * on the satellite.</p>
     */
    public static void ensureNoThickLvmSnapshots(VolumeDefinition vlmDfnRef)
    {
        ResourceDefinition rscDfn = vlmDfnRef.getResourceDefinition();
        for (SnapshotDefinition snapshotDfn : rscDfn.getSnapshotDfns())
        {
            // snapshots stuck in deleting could prevent a later lvresize
            if (!snapshotDfn.isDeleted())
            {
                for (Snapshot snapshot : snapshotDfn.getAllSnapshots())
                {
                    @Nullable SnapshotVolume snapshotVlm = snapshot.getVolume(vlmDfnRef.getVolumeNumber());
                    if (snapshotVlm != null && snapshot.getFlags().isUnset(Snapshot.Flags.DELETE))
                    {
                        Map<String, StorPool> storPoolMap = LayerVlmUtils.getStorPoolMap(snapshotVlm);
                        for (StorPool storPool : storPoolMap.values())
                        {
                            if (storPool.getDeviceProviderKind().equals(DeviceProviderKind.LVM))
                            {
                                throw new ApiRcException(
                                    ApiCallRcImpl.simpleEntry(
                                        ApiConsts.FAIL_EXISTS_SNAPSHOT,
                                        "Volume definition " + vlmDfnRef.getVolumeNumber() + " of '" +
                                            rscDfn.getName() + "' cannot be resized because snapshot '" +
                                            snapshot.getSnapshotName() + "' exists on node '" +
                                            snapshot.getNodeName() + "' on thick LVM storage. " +
                                            "Delete the snapshot(s) of this resource first."
                                    )
                                        .setSkipErrorReport(true)
                                );
                            }
                        }
                    }
                }
            }
        }
    }
}
