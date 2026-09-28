package com.linbit.linstor.systemstarter;

import com.linbit.SystemService;
import com.linbit.SystemServiceStartException;
import com.linbit.linstor.InitializationException;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.dbdrivers.DatabaseException;

public interface StartupInitializer
{
    void initialize()
        throws InitializationException,
        DatabaseException,
        SystemServiceStartException;

    /**
     * Shuts down this initializer.
     *
     * @param jvmShutdownRef <code>True</code> iff this shutdown was initiated by the shutdown hook or another
     *      source that should end in the end of the JVM relatively soon.
     */
    default void shutdown(boolean jvmShutdownRef)
    {
    }

    default void awaitShutdown(long timeout) throws InterruptedException
    {
    }

    default @Nullable SystemService getSystemService()
    {
        return null;
    }
}
