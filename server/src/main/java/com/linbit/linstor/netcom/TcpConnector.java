package com.linbit.linstor.netcom;

import com.linbit.SystemService;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.objects.Node;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Server for TCP/IP connections to various other servers and clients
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface TcpConnector extends SystemService
{
    // TODO: Experimental; some means of adding a new connection that is to
    //       be created by the TcpConnector implementation
    Peer connect(InetSocketAddress address, Node node, @Nullable Object initialConnectSinkKey) throws IOException;

    Peer reconnect(Peer peer) throws IOException;

    /**
     * Close the connection to the peer and do not automatically reconnect.
     */
    void closeConnection(TcpConnectorPeer peerObj, boolean allowReconnect);

    /**
     * Wakes up the connector's selector
     */
    void wakeup();
}
