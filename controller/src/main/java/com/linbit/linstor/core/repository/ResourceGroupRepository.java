package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.ResourceGroupName;
import com.linbit.linstor.core.objects.ResourceGroup;

/**
 * Provides access to {@link KeyValueStore}s.
 */
public interface ResourceGroupRepository
{
    @Nullable
    ResourceGroup get(ResourceGroupName nameRef);

    void put(ResourceGroup rscGrpData);

    void remove(ResourceGroupName rscGrpName);

    CoreModule.ResourceGroupMap getMapForView();
}
