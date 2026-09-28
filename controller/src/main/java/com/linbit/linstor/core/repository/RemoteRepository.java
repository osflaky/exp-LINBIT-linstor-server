package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.identifier.RemoteName;
import com.linbit.linstor.core.objects.remotes.AbsRemote;
import com.linbit.linstor.core.objects.remotes.S3Remote;

public interface RemoteRepository
{
    @Nullable
    AbsRemote get(RemoteName remoteName);

    void put(AbsRemote remote);

    void remove(RemoteName remoteName);

    CoreModule.RemoteMap getMapForView();

    default @Nullable S3Remote getS3(RemoteName remoteName)
    {
        AbsRemote remote = get(remoteName);
        if (remote != null && !(remote instanceof S3Remote))
        {
            throw new ApiRcException(
                ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_INVLD_REMOTE_NAME,
                    "Given Remote was not of class S3Remote but of " + remote.getClass().getCanonicalName()
                )
            );
        }
        return (S3Remote) remote;
    }
}
