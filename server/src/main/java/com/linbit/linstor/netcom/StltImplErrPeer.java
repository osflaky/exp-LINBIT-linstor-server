package com.linbit.linstor.netcom;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.ServiceName;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.prop.Property;
import com.linbit.linstor.core.cfg.StltConfig;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.satellitestate.SatelliteState;
import com.linbit.linstor.utils.externaltools.ExtToolsManager;

import javax.net.ssl.SSLException;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class StltImplErrPeer implements Peer
{
    private static final String ERROR_MSG = "This peer is not supposed to be accessed.";
    static ServiceName serviceName;

    static
    {
        try
        {
            serviceName = new ServiceName("StltImplErrPeer");
        }
        catch (InvalidNameException ignored)
        {
        }
    }

    public StltImplErrPeer()
    {
    }

    @Override
    public String getId()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public InetSocketAddress getHostAddr()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Node getNode()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ServiceName getConnectorInstanceName()
    {
        return serviceName;
    }

    @Override
    public void attach(Object ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Object getAttachment()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Message createMessage()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean sendMessage(Message ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean sendMessage(byte[] ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long getNextIncomingMessageSeq()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void processInOrder(long ingored1, Publisher<?> ignored2)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Flux<ByteArrayInputStream> apiCall(String ignored1, byte[] ignored2)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void apiCallAnswer(long ignored1, ByteArrayInputStream ignored2)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void apiCallError(long ignored1, Throwable ignored2)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void apiCallComplete(long ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setAllowReconnect(boolean ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean isAllowReconnect()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void closeConnection()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void closeConnection(boolean ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void connectionClosing()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean isOnline()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ApiConsts.ConnectionStatus getConnectionStatus()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setConnectionStatus(ApiConsts.ConnectionStatus ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ApiConsts.Platform getPlatform()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setPlatform(ApiConsts.Platform ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public String getOsVariant()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setOsVariant(String ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean isConnected(boolean ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean isAuthenticated()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setAuthenticated(boolean ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public int outQueueCapacity()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public int outQueueCount()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long msgSentCount()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long msgRecvCount()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long msgSentMaxSize()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long msgRecvMaxSize()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public InetSocketAddress peerAddress()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public InetSocketAddress localAddress()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void connectionEstablished() throws SSLException
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void waitUntilConnectionEstablished() throws InterruptedException
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public TcpConnector getConnector()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void sendPing()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void sendPong()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void pongReceived()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long getLastPingSent()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long getLastPongReceived()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ReadWriteLock getSatelliteStateLock()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public SatelliteState getSatelliteState()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ReadWriteLock getSerializerLock()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setFullSyncId(long ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long getFullSyncId()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public long getNextSerializerId()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void fullSyncFailed(ApiConsts.ConnectionStatus ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean hasFullSyncFailed()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void fullSyncApplied()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean isFullSyncApplied()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public boolean hasNextMsgIn()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Message nextCurrentMsgIn()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public ExtToolsManager getExtToolsManager()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public StltConfig getStltConfig()
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setStltConfig(StltConfig ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public void setDynamicProperties(List<Property> ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }

    @Override
    public Property getDynamicProperty(String ignored)
    {
        throw new ImplementationError(ERROR_MSG);
    }
}
