package com.linbit.linstor.core.apicallhandler;

import com.google.inject.AbstractModule;
import com.linbit.linstor.api.interfaces.serializer.CommonSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.api.protobuf.serializer.ProtoCommonSerializer;
import com.linbit.linstor.api.protobuf.serializer.ProtoCtrlStltSerializer;

public class ApiCallHandlerModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(CommonSerializer.class).to(ProtoCommonSerializer.class);
        bind(CtrlStltSerializer.class).to(ProtoCtrlStltSerializer.class);
    }
}
