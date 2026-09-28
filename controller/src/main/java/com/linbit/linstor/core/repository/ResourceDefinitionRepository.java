package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.ResourceName;
import com.linbit.linstor.core.objects.ResourceDefinition;

/**
 * Provides access to resource definitions.
 */
public interface ResourceDefinitionRepository
{
    @Nullable
    ResourceDefinition get(ResourceName nameRef);

    @Nullable
    ResourceDefinition get(byte[] externalName);

    void put(ResourceDefinition resourceDefinition);

    void remove(ResourceName resourceName, byte[] externalName);

    CoreModule.ResourceDefinitionMap getMapForView();

    CoreModule.ResourceDefinitionMapExtName getMapForViewExtName();
}
