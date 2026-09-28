package com.linbit.linstor.core.repository;

import com.linbit.linstor.core.ControllerCoreModule;
import com.linbit.linstor.core.identifier.SharedStorPoolName;
import com.linbit.linstor.core.objects.FreeSpaceMgr;

/**
 * Provides access to free space managers.
 */
public interface FreeSpaceMgrRepository
{
    FreeSpaceMgr get(SharedStorPoolName sharedStorPoolName);

    void put(SharedStorPoolName sharedStorPoolName, FreeSpaceMgr freeSpaceMgr);

    void remove(SharedStorPoolName sharedStorPoolName);

    ControllerCoreModule.FreeSpaceMgrMap getMapForView();
}
