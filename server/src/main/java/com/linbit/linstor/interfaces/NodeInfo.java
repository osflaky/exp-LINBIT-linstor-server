package com.linbit.linstor.interfaces;

import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.propscon.ReadOnlyProps;

import java.util.UUID;

/*
 * TODO this interface should be in the satellite project, not server.
 *
 * The only reason it is still in the server-project are the serializer-classes. For some reason satellite specific
 * methods (i.e. methods that only the satellite can send to the controller, not vice versa) are still in the server-
 * project instead of the satellite project.
 */
public interface NodeInfo
{
    UUID getUuid();

    NodeName getName();

    ReadOnlyProps getReadOnlyProps();
}
