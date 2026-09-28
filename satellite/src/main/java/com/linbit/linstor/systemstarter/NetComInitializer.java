package com.linbit.linstor.systemstarter;

import com.linbit.SystemServiceStartException;
import com.linbit.linstor.core.SatelliteNetComInitializer;

public class NetComInitializer implements StartupInitializer
{
    private SatelliteNetComInitializer sncInitializer;

    public NetComInitializer(
        SatelliteNetComInitializer sncInitializerRef
    )
    {
        sncInitializer = sncInitializerRef;
    }

    @Override
    public void initialize() throws SystemServiceStartException
    {
        if (!sncInitializer.initMainNetComService())
        {
            throw new SystemServiceStartException("Initialisation of SatelliteNetComServices failed.", true);
        }
    }

    @Override
    public void shutdown(boolean jvmShutdownRef)
    {
        sncInitializer.shutdown(jvmShutdownRef);
    }

    @Override
    public void awaitShutdown(long timeout) throws InterruptedException
    {
        sncInitializer.awaitShutdown(timeout);
    }
}
