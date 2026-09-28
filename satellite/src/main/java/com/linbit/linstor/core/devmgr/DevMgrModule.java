package com.linbit.linstor.core.devmgr;

import com.linbit.linstor.core.DeviceManager;
import com.linbit.linstor.core.UpdateMonitor;
import com.linbit.linstor.core.UpdateMonitorImpl;
import com.linbit.linstor.layer.DeviceLayer.NotificationListener;


import com.google.inject.AbstractModule;

public class DevMgrModule extends AbstractModule
{
    public static final String STLT_CONF_LOCK = "stltConfLock";
    public static final String DRBD_CONFIG_PATH = "DrbdConfigPath";

    @Override
    protected void configure()
    {
        bind(UpdateMonitor.class).to(UpdateMonitorImpl.class);
        // bind(DeviceManager.class).to(DeviceManagerImpl.class);
        // install(new FactoryModuleBuilder()
        //     .implement(DeviceManagerImpl.DeviceHandlerInvocation.class,
        //         DeviceManagerImpl.DeviceHandlerInvocation.class)
        //     .build(DeviceManagerImpl.DeviceHandlerInvocationFactory.class));
        bind(DeviceManager.class).to(DeviceManagerImpl.class);
        bind(NotificationListener.class).to(DeviceManagerImpl.class);
        bind(DeviceHandler.class).to(DeviceHandlerImpl.class);
    }

}
