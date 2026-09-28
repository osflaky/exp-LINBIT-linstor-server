package com.linbit.linstor.core;

import com.linbit.linstor.core.apicallhandler.controller.FreeCapacityFetcher;
import com.linbit.linstor.core.apicallhandler.controller.FreeCapacityFetcherProto;
import com.linbit.linstor.core.apicallhandler.controller.VlmAllocatedFetcher;
import com.linbit.linstor.core.apicallhandler.controller.VlmAllocatedFetcherProto;

import com.google.inject.AbstractModule;

public class ControllerSatelliteCommunicationModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(SatelliteConnector.class).to(SatelliteConnectorImpl.class);
        bind(FreeCapacityFetcher.class).to(FreeCapacityFetcherProto.class);
        bind(VlmAllocatedFetcher.class).to(VlmAllocatedFetcherProto.class);
    }
}
