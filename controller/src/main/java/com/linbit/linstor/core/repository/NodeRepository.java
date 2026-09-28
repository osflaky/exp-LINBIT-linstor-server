package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.objects.Node;

/**
 * Provides access to nodes.
 */
public interface NodeRepository
{
    @Nullable
    Node get(NodeName nodeName);

    void put(NodeName nodeName, Node node);

    void remove(NodeName nodeName);

    CoreModule.NodesMap getMapForView();

    void putUname(String uName, NodeName nodeName);
    @Nullable NodeName getUname(String uName);
    void removeUname(String uName);
}
