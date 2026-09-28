package com.linbit.linstor.core.apicallhandler.controller.utils;

import com.linbit.linstor.core.identifier.ResourceName;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.layer.storage.ebs.EbsUtils;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.satellitestate.SatelliteResourceState;
import com.linbit.linstor.satellitestate.SatelliteState;
import com.linbit.linstor.satellitestate.SatelliteVolumeState;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.utils.layer.LayerRscUtils;

import java.util.Collection;

public class SatelliteResourceStateDrbdUtils
{
    public static boolean allResourcesUpToDate(Collection<Node> nodes, ResourceName rscName)
    {
        boolean ret = true;
        for (Node node : nodes)
        {
            Resource rsc = node.getResource(rscName);
            // list of nodes might have been created from a list of snapshot, where the corresponding
            // resource is already deleted
            if (rsc != null && !rsc.isDeleted() && !SatelliteResourceStateDrbdUtils.allVolumesUpToDate(rsc))
            {
                ret = false;
                break;
            }
        }
        return ret;
    }

    public static boolean allVolumesUpToDate(Resource rsc)
    {
        return allVolumesUpToDate(rsc, true);
    }

    public static boolean allVolumesUpToDate(Resource rsc, boolean defaultIfUnknown)
    {
        boolean ret = defaultIfUnknown;

        boolean checkState = LayerRscUtils.getLayerStack(rsc).contains(DeviceLayerKind.DRBD);
        // do not check EBS target resource, only initiator resource
        checkState &= (!EbsUtils.hasEbsVlms(rsc) ||
            rsc.getStateFlags().isSet(Resource.Flags.EBS_INITIATOR));

        checkState &= !rsc.getStateFlags().isSet(Resource.Flags.DRBD_DISKLESS);

        if (checkState)
        {
            Peer peer = rsc.getNode().getPeer();
            ResourceName rscName = rsc.getResourceDefinition().getName();
            SatelliteState stltState = peer.getSatelliteState();
            if (stltState != null)
            {
                SatelliteResourceState rscState = stltState.getResourceStates().get(rscName);
                if (rscState != null)
                {
                    Collection<SatelliteVolumeState> vlmStates = rscState.getVolumeStates().values();
                    if (vlmStates != null)
                    {
                        ret = true;
                        for (SatelliteVolumeState stltVlmStates : vlmStates)
                        {
                            if (!stltVlmStates.getDiskState().equalsIgnoreCase("uptodate"))
                            {
                                ret = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }
}
