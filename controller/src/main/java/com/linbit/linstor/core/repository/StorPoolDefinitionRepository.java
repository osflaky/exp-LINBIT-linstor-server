package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.StorPoolName;
import com.linbit.linstor.core.objects.StorPoolDefinition;

/**
 * Provides access to stor pool definitions.
 */
public interface StorPoolDefinitionRepository
{
    @Nullable
    StorPoolDefinition get(StorPoolName nameRef);

    void put(StorPoolName storPoolName, StorPoolDefinition storPoolDefinition);

    void remove(StorPoolName storPoolName);

    CoreModule.StorPoolDefinitionMap getMapForView();
}
