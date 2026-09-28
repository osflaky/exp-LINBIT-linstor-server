package com.linbit.linstor.core;

import com.linbit.linstor.api.interfaces.serializer.CommonSerializer;
import com.linbit.linstor.api.interfaces.serializer.CommonSerializer.CommonSerializerBuilder;
import com.linbit.linstor.core.apicallhandler.StltApiCallHandlerUtils;
import com.linbit.linstor.core.apicallhandler.StltExtToolsChecker;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.objects.NodeSatelliteFactory;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.transaction.manager.TransactionMgr;
import com.linbit.linstor.utils.externaltools.ExtToolsManager;

import java.util.Collections;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.mockito.Answers;

/**
 * Tests the connection handling of {@link ControllerPeerConnectorImpl#setControllerPeer}.
 *
 * <p>
 * During a reconnect the controller may send more than one Auth message (e.g. one triggered by the
 * ReconnectorTask and one by the connection-established callback). If both Auth messages arrive over the SAME
 * (current) connection, the satellite must not close that connection - otherwise the satellite kills its only
 * living controller connection.
 * </p>
 */
public class ControllerPeerConnectorImplTest
{
    private static final UUID CTRL_UUID = UUID.randomUUID();
    private static final UUID NODE_UUID = UUID.randomUUID();
    private static final String NODE_NAME = "node1";

    private ControllerPeerConnectorImpl peerConnector;

    @Before
    public void setUp() throws Exception
    {
        Node localNode = mock(Node.class);
        when(localNode.getName()).thenReturn(new NodeName(NODE_NAME));

        NodeSatelliteFactory nodeFactory = mock(NodeSatelliteFactory.class);
        when(nodeFactory.getInstanceSatellite(any(), any(), any(), any())).thenReturn(localNode);

        CommonSerializerBuilder serializerBuilder = mock(
            CommonSerializerBuilder.class,
            withSettings().defaultAnswer(Answers.RETURNS_SELF)
        );
        when(serializerBuilder.build()).thenReturn(new byte[0]);
        CommonSerializer commonSerializer = mock(CommonSerializer.class);
        when(commonSerializer.onewayBuilder(anyString())).thenReturn(serializerBuilder);

        StltExtToolsChecker extToolsChecker = mock(StltExtToolsChecker.class);
        when(extToolsChecker.getExternalTools(anyBoolean())).thenReturn(Collections.emptyMap());

        TransactionMgr transMgr = mock(TransactionMgr.class);

        peerConnector = new ControllerPeerConnectorImpl(
            new TestNodesMap(),
            new ReentrantReadWriteLock(true),
            new ReentrantReadWriteLock(true),
            new ReentrantReadWriteLock(true),
            new ReentrantReadWriteLock(true),
            mock(ErrorReporter.class),
            nodeFactory,
            () -> transMgr,
            commonSerializer,
            mock(StltConnTracker.class),
            () -> mock(StltApiCallHandlerUtils.class),
            extToolsChecker
        );
    }

    @Test
    public void reAuthOverCurrentConnectionMustNotCloseIt()
    {
        Peer peer = mockPeer("peer1");

        peerConnector.setControllerPeer(CTRL_UUID, peer, NODE_UUID, NODE_NAME);
        verify(peer, never()).closeConnection();

        // second Auth over the same, still living connection (double reconnect)
        peerConnector.setControllerPeer(CTRL_UUID, peer, NODE_UUID, NODE_NAME);

        verify(peer, never()).closeConnection();
        assertSame(peer, peerConnector.getControllerPeer());
    }

    @Test
    public void newConnectionClosesPreviousConnection()
    {
        Peer stalePeer = mockPeer("stalePeer");
        Peer freshPeer = mockPeer("freshPeer");

        peerConnector.setControllerPeer(CTRL_UUID, stalePeer, NODE_UUID, NODE_NAME);
        peerConnector.setControllerPeer(CTRL_UUID, freshPeer, NODE_UUID, NODE_NAME);

        verify(stalePeer).closeConnection();
        verify(freshPeer, never()).closeConnection();
        assertSame(freshPeer, peerConnector.getControllerPeer());
    }

    private Peer mockPeer(String peerId)
    {
        Peer peer = mock(Peer.class, peerId);
        when(peer.getId()).thenReturn(peerId);
        when(peer.getExtToolsManager()).thenReturn(mock(ExtToolsManager.class));
        return peer;
    }

    @SuppressWarnings("serial")
    private static final class TestNodesMap extends TreeMap<NodeName, Node> implements CoreModule.NodesMap
    {
    }
}
