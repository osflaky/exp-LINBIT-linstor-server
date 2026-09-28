package com.linbit.linstor.core.apicallhandler.controller.autohelper;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.interfaces.AutoSelectFilterApi;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.ResourceDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import reactor.core.publisher.Flux;

public class AutoHelperContext
{
    /*
     * Input from the caller
     */
    final ApiCallRcImpl responses;
    final ResponseContext responseContext;
    final ResourceDefinition rscDfn;

    @Nullable AutoSelectFilterApi selectFilter;

    /*
     * CtrlRscAutoRePlaceHelper specific fields
     */
    List<ResourceDefinition> needRePlaceRsc = new ArrayList<>();

    /*
     * Internal context / state.
     */
    TreeSet<Resource> resourcesToCreate = new TreeSet<>();
    TreeSet<NodeName> nodeNamesForDelete = new TreeSet<>();

    List<Flux<ApiCallRc>> additionalFluxList = new ArrayList<>();

    boolean requiresUpdateFlux = false;

    boolean preventUpdateSatellitesForResourceDelete = false;
    boolean keepTiebreaker;


    public AutoHelperContext(
        ApiCallRcImpl responsesRef,
        ResponseContext contextRef,
        ResourceDefinition definitionRef
    )
    {
        responses = responsesRef;
        responseContext = contextRef;
        rscDfn = definitionRef;
    }

    public AutoHelperContext withSelectFilter(AutoSelectFilterApi selectFilterRef)
    {
        selectFilter = selectFilterRef;
        return this;
    }

    public AutoHelperContext withKeepTiebreaker(boolean keepTiebreakerRef)
    {
        keepTiebreaker = keepTiebreakerRef;
        return this;
    }

    public void addNeedRePlaceRsc(Resource rscRef)
    {
        needRePlaceRsc.add(rscRef.getResourceDefinition());
    }
}
