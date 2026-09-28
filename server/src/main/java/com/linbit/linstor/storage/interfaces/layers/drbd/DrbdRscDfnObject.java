package com.linbit.linstor.storage.interfaces.layers.drbd;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.types.TcpPortNumber;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.storage.interfaces.categories.resource.RscDfnLayerObject;

public interface DrbdRscDfnObject extends RscDfnLayerObject
{
    @Nullable
    TcpPortNumber getTcpPort();

    TransportType getTransportType();

    String getSecret();

    short getPeerSlots();

    int getAlStripes();

    long getAlStripeSize();

    boolean isDown();

    void setDown(boolean downRef) throws DatabaseException;

    enum TransportType
    {
        IP, RDMA, RoCE;

        public static TransportType byValue(String str) throws IllegalArgumentException
        {
            return switch (str.toUpperCase())
            {
                case "IP" -> IP;
                case "RDMA" -> RDMA;
                case "ROCE" -> RoCE;
                default -> throw new IllegalArgumentException(
                    "Unknown TransportType: '" + str + "'"
                );
            };
        }

        public static TransportType valueOfIgnoreCase(String string, TransportType defaultValue)
            throws IllegalArgumentException
        {
            TransportType ret = defaultValue;
            if (string != null)
            {
                ret = valueOf(string.toUpperCase());
            }
            return ret;
        }
    }
}
