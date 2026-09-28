package com.linbit.linstor.core;

import com.linbit.linstor.InitializationException;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.logging.ErrorReporter;

import info.faljse.SDNotify.SDNotify;

public class SystemdUtils
{
    private SystemdUtils()
    {
        /* This utility class should not be instantiated */
    }

    public static void notifyReady(ErrorReporter errorReporteRef) throws InitializationException
    {
        try
        {
            @Nullable String notifySocket = System.getenv("NOTIFY_SOCKET");
            if (notifySocket != null && !notifySocket.trim().isEmpty())
            {
                SDNotify.sendNotify();
            }
            else
            {
                errorReporteRef.logWarning(
                    "Not calling 'systemd-notify' as NOTIFY_SOCKET is %s",
                    notifySocket == null ? "null" : "empty"
                );
            }
        }
        catch (Exception exc)
        {
            throw new InitializationException(exc);
        }
    }
}
