package com.linbit.linstor.core.apicallhandler.controller.autohelper;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;

import reactor.core.publisher.Flux;

public record AutoHelperResult(
    Flux<ApiCallRc> flux,
    ApiCallRcImpl responses,
    boolean preventUpdateSatellitesForResourceDelete
)
{
}
