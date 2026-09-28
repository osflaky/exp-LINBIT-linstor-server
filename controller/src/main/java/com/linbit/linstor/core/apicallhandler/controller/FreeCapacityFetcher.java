package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.SpaceInfo;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.objects.StorPool;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface FreeCapacityFetcher
{
    Mono<Map<StorPool.Key, Long>> fetchThinFreeCapacities(Set<NodeName> nodesFilter);

    Mono<Map<StorPool.Key, Tuple2<SpaceInfo, List<ApiCallRc>>>> fetchThinFreeSpaceInfo(Set<NodeName> nodesFilter);

    /**
     * Like {@link #fetchThinFreeSpaceInfo(Set)}, but selects the nodes to query by matching their names
     * against the given regex patterns (empty list = all nodes). This avoids querying every satellite when
     * the caller only has regex name filters.
     */
    Mono<Map<StorPool.Key, Tuple2<SpaceInfo, List<ApiCallRc>>>> fetchThinFreeSpaceInfo(
        List<Pattern> nodeNameFilters
    );
}
