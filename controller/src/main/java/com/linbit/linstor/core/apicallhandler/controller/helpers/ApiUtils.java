package com.linbit.linstor.core.apicallhandler.controller.helpers;


public class ApiUtils
{
    public interface AccessCheckedRunnable<T>
    {
        T execPrivileged();
    }

    public static <T> T execPrivileged(AccessCheckedRunnable<T> runnable)
    {
        T ret;
        ret = runnable.execPrivileged();
        return ret;
    }

    private ApiUtils()
    {
    }
}
