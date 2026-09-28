package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ImplementationError;
import com.linbit.linstor.PriorityProps;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiException;
import com.linbit.linstor.core.identifier.StorPoolName;
import com.linbit.linstor.core.objects.AbsResource;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.objects.SnapshotVolume;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.ReadOnlyProps;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class FreeCapacityAutoPoolSelectorUtils
{
    /**
     * Returns whether the given storage pool is usable in terms of capacity and provisioning type.
     * If the free capacity is unknown, an empty Optional is returned.
     */
    public static Optional<Boolean> isStorPoolUsable(
        long rscSize,
        @Nullable Map<StorPool.Key, Long> freeCapacities,
        boolean includeThin,
        StorPoolName storPoolName,
        Node node,
        ReadOnlyProps ctrlPropsRef
    )
    {
        StorPool storPool = getStorPoolPrivileged(node, storPoolName);

        Optional<Boolean> usable;
        if (storPool.getDeviceProviderKind().usesThinProvisioning() && !includeThin)
        {
            usable = Optional.of(false);
        }
        else
        {
            usable = getFreeCapacityCurrentEstimationPrivileged(
                freeCapacities,
                storPool,
                ctrlPropsRef,
                true
            ).map(freeCapacity -> freeCapacity >= rscSize);
        }
        return usable;
    }

    private static StorPool getStorPoolPrivileged(
        Node nodeRef,
        StorPoolName storPoolNameRef
    )
    {
        StorPool storPool;
        storPool = nodeRef.getStorPool(storPoolNameRef);
        return storPool;
    }

    /**
     * The current estimation of free capacity is calculated as follows:
     *
     * There are two separate calculations made in the background:
     * 1) (Storage pool's free space) * (MaxFreeCapacityOversubscriptionRatio)
     * 2) (Storage pool's total capacity) * (MaxTotalCapacityOversubscriptionRatio)
     *
     * If both values can be calculated, the lower of the two is returned by this method.
     * If some information is missing such that at least one of the values cannot be calculated (due to missing updates
     * from the satellite i.e. regarding free space), an Optional.empty() is returned.
     *
     * The properties "MaxFreeCapacityOversubscriptionRatio" and "MaxTotalCapacityOversubscriptionRatio" will each
     * default to "MaxOversubscriptionRatio" if they do not exist. If "MaxOversubscriptionRatio" itself does not exist,
     * the {@link StorPool} class has rules for default values, which are then used for the above properties.
     *
     *
     */
    public static Optional<Long> getFreeCapacityCurrentEstimationPrivileged(
        @Nullable Map<StorPool.Key, Long> thinFreeCapacities,
        StorPool storPoolRef,
        ReadOnlyProps ctrlPropRef,
        boolean includeOversubscriptionRatioRef
    )
    {
        final Optional<Long> ret;
        final Long freeSpace = getFreeSpacePrivileged(thinFreeCapacities, storPoolRef);
        if (!includeOversubscriptionRatioRef)
        {
            ret = Optional.ofNullable(freeSpace);
        }
        else
        {
            Long capacity = getCapacityPrivilged(storPoolRef);
            if (freeSpace == null || capacity == null)
            {
                ret = Optional.empty();
            }
            else
            {
                final double maxOversubRatio = getMaxOversubscriptionRatioPrivileged(
                    storPoolRef,
                    ctrlPropRef
                );
                Double maxFreeRatio = getFreeCapacityOversubscriptionRatioPrivileged(
                    storPoolRef,
                    ctrlPropRef
                );
                if (maxFreeRatio == null)
                {
                    maxFreeRatio = maxOversubRatio;
                }
                Double maxCapacityRatio = getTotalCapacityOversubscriptionRatioPrivileged(
                    storPoolRef,
                    ctrlPropRef
                );
                if (maxCapacityRatio == null)
                {
                    maxCapacityRatio = maxOversubRatio;
                }

                final long freeSpaceBased = calculateOverProvisionedSpace(freeSpace, maxFreeRatio);

                final long reservedCapacity = getReservedCapacityPrivileged(storPoolRef);
                final long totalSpaceBased = calculateOverProvisionedSpace(capacity, maxCapacityRatio);

                ret = Optional.of(
                    Math.min(
                        freeSpaceBased,
                        totalSpaceBased - reservedCapacity
                    )
                );
            }
        }
        return ret;
    }

    private static long calculateOverProvisionedSpace(long spaceRef, double ratioRef)
    {
        final long ret;
        if (spaceRef == Long.MAX_VALUE || Long.MAX_VALUE / ratioRef < spaceRef)
        {
            ret = Long.MAX_VALUE; // prevent overflow
        }
        else
        {
            ret = (long) (spaceRef * ratioRef);
        }
        return ret;
    }

    /**
     * If thinFreeCapacitiesRef is not null, it's value will be returned for the given storPool (if it exists).
     * Otherwise the storPool's freeSpaceTracker is asked for the free space. If that is also not set, null is returned.
     *
     *
     */
    private static @Nullable Long getFreeSpacePrivileged(
        @Nullable Map<StorPool.Key, Long> thinFreeCapacitiesRef,
        StorPool storPoolRef
    )
    {
        Long ret = null;
        if (thinFreeCapacitiesRef != null)
        {
            ret = thinFreeCapacitiesRef.get(storPoolRef.getKey());
        }
        if (ret == null)
        {
            ret = storPoolRef.getFreeSpaceTracker().getFreeCapacityLastUpdated().orElse(null);
        }
        return ret;
    }

    private static @Nullable Long getCapacityPrivilged(StorPool storPoolRef)
    {
        return storPoolRef.getFreeSpaceTracker().getTotalCapacity().orElse(null);
    }

    private static long getReservedCapacityPrivileged(StorPool storPoolRef)
    {
        long reservedCapacity;
        reservedCapacity = storPoolRef.getFreeSpaceTracker().getPendingAllocatedSum();
        reservedCapacity += getReservedSum(storPoolRef.getVolumes());
        reservedCapacity += getReservedSum(storPoolRef.getSnapVolumes());
        return reservedCapacity;
    }

    private static <RSC extends AbsResource<RSC>> long getReservedSum(
        Collection<VlmProviderObject<RSC>> absVlmListRef
    )
    {
        long ret = 0;
        for (VlmProviderObject<RSC> absVlmData : absVlmListRef)
        {
            if (absVlmData.getVolume() instanceof SnapshotVolume)
            {
                ret += absVlmData.getAllocatedSize();
            }
            else
            {
                ret += absVlmData.getUsableSize();
            }
        }
        return ret;
    }

    private static @Nullable Double getRatioPrivileged(
        StorPool storPoolRef,
        ReadOnlyProps ctrlPropRef,
        String propKeyRef
    )
    {
        final Double ret;
        String val = null;
        try
        {
            val = new PriorityProps(
                storPoolRef.getProps(),
                storPoolRef.getDefinition().getProps(),
                ctrlPropRef
            ).getProp(propKeyRef);
            if (val != null)
            {
                ret = Double.parseDouble(val);
            }
            else
            {
                ret = null;
            }
        }
        catch (InvalidKeyException exc)
        {
            throw new ImplementationError(exc);
        }
        catch (NumberFormatException nfe)
        {
            throw new ApiException("Unable to parse number. Property: " + propKeyRef + ". Value: " + val, nfe);
        }
        return ret;
    }

    public static double getMaxOversubscriptionRatioPrivileged(
        StorPool storPoolRef,
        @Nullable ReadOnlyProps ctrlPropsRef
    )
    {
        double osRatio;
        osRatio = storPoolRef.getOversubscriptionRatio(ctrlPropsRef);
        return osRatio;
    }

    public static @Nullable Double getFreeCapacityOversubscriptionRatioPrivileged(
        StorPool storPoolRef,
        ReadOnlyProps ctrlPropRef
    )
    {
        return getRatioPrivileged(
            storPoolRef,
            ctrlPropRef,
            ApiConsts.KEY_STOR_POOL_MAX_FREE_CAPACITY_OVERSUBSCRIPTION_RATIO
        );
    }

    public static @Nullable Double getTotalCapacityOversubscriptionRatioPrivileged(
        StorPool storPoolRef,
        ReadOnlyProps ctrlPropRef
    )
    {
        return getRatioPrivileged(
            storPoolRef,
            ctrlPropRef,
            ApiConsts.KEY_STOR_POOL_MAX_TOTAL_CAPACITY_OVERSUBSCRIPTION_RATIO
        );
    }

    private FreeCapacityAutoPoolSelectorUtils()
    {
    }
}
