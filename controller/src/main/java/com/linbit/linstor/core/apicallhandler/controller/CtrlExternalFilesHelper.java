package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.identifier.ExternalFileName;
import com.linbit.linstor.core.objects.ExternalFile;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.InvalidValueException;
import com.linbit.linstor.propscon.Props;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CtrlExternalFilesHelper
{
    private CtrlExternalFilesHelper()
    {
    }

    public static void deployPath(Props propsRef, ExternalFile extFileRef)
        throws InvalidKeyException, DatabaseException, InvalidValueException
    {
        propsRef.setProp(
            InternalApiConsts.NAMESPC_FILES + "/" + extFileRef.getName().extFileName,
            ApiConsts.VAL_TRUE
        );
    }

    // public static void undeployPath(Props writableProps, ExternalFile extFile)
    // throws InvalidKeyException, DatabaseException, InvalidValueException
    // {
    // writableProps.setProp(
    // InternalApiConsts.NAMESPC_FILES + "/" + extFile.getName().extFileName,
    // ApiConsts.VAL_FALSE
    // );
    // }

    public static String removePath(Props writableProps, ExternalFile extFile)
        throws InvalidKeyException, DatabaseException
    {
        return writableProps.removeProp(
            InternalApiConsts.NAMESPC_FILES + "/" + extFile.getName().extFileName
        );
    }

    public static boolean isPathWhitelisted(
        ExternalFileName extFileName,
        Node node
    )
    {
        Path extFilePath = Paths.get(extFileName.extFileName).normalize();
        return node.getPeer().getStltConfig().getWhitelistedExternalFilePaths().contains(extFilePath.getParent());
    }
}
