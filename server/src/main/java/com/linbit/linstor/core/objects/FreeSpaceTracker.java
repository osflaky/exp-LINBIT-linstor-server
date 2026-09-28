package com.linbit.linstor.core.objects;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.identifier.SharedStorPoolName;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;
import com.linbit.linstor.transaction.TransactionObject;

import java.util.Optional;

public interface FreeSpaceTracker extends TransactionObject
{
    SharedStorPoolName getName();

    void vlmCreating(VlmProviderObject<?> vlmProviderObjRef);

    void ensureVlmNoLongerCreating(
        VlmProviderObject<?> vlmProviderObjRef
    );

    void vlmCreationFinished(
        VlmProviderObject<?> vlmProviderObjRef,
        @Nullable Long freeCapacityRef,
        @Nullable Long totalCapacityRef
    );

    Optional<Long> getFreeCapacityLastUpdated();

    Optional<Long> getTotalCapacity();

    long getPendingAllocatedSum();

    void setCapacityInfo(long freeSpaceRef, long totalCapacity);
}
