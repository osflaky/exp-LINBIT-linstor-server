package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.identifier.ResourceName;
import com.linbit.linstor.core.identifier.StorPoolName;
import com.linbit.linstor.core.objects.Volume;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import reactor.core.publisher.Mono;

public interface VlmAllocatedFetcher
{
    Mono<Map<Volume.Key, VlmAllocatedResult>> fetchVlmAllocated(
        Set<NodeName> nodesFilter,
        Set<StorPoolName> storPoolFilter,
        Set<ResourceName> resourceFilter
    );

    /**
     * Like {@link #fetchVlmAllocated(Set, Set, Set)}, but selects the nodes to query by matching their
     * names against the given regex patterns (empty list = all nodes). This avoids querying every
     * satellite when the caller only has regex node-name filters. The storage-pool and resource filters
     * keep the exact-name {@link Set} semantics.
     */
    Mono<Map<Volume.Key, VlmAllocatedResult>> fetchVlmAllocated(
        List<Pattern> nodeNameFilters,
        Set<StorPoolName> storPoolFilter,
        Set<ResourceName> resourceFilter
    );
}
