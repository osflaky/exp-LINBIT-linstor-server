package com.linbit.linstor.debug;

import com.linbit.InvalidNameException;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.StorPoolName;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.Snapshot;
import com.linbit.linstor.core.objects.SnapshotVolume;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.core.objects.StorPoolDefinition;
import com.linbit.linstor.core.objects.Volume;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;
import com.linbit.utils.TreePrinter;
import com.linbit.utils.UuidUtils;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class CmdDisplayStorPool extends BaseDebugCmd
{
    private static final Map<String, String> PARAMETER_DESCRIPTIONS = new TreeMap<>();

    private final FilteredObjectLister<StorPoolDefinition> lister;

    static
    {
        PARAMETER_DESCRIPTIONS.put(
            FilteredObjectLister.PRM_NAME,
            "Name of the storage pool(s) to display"
        );
        PARAMETER_DESCRIPTIONS.put(
            FilteredObjectLister.PRM_FILTER_NAME,
            "Filter pattern to apply to the storage pool name.\n" +
            "Storage pools with a name matching the pattern will be displayed."
        );
    }

    private final ReadWriteLock reconfigurationLock;
    private final ReadWriteLock storPoolDfnMapLock;
    private final CoreModule.StorPoolDefinitionMap storPoolDfnMap;

    public CmdDisplayStorPool(
        ReadWriteLock reconfigurationLockRef,
        ReadWriteLock storPoolDfnMapLockRef,
        CoreModule.StorPoolDefinitionMap storPoolDfnMapRef
    )
    {
        super(
            new String[]
            {
                "DspStorPool"
            },
            "Display storage pool(s)",
            "Displays information about one or multiple storage pool(s)",
            PARAMETER_DESCRIPTIONS,
            null
        );

        reconfigurationLock = reconfigurationLockRef;
        storPoolDfnMapLock = storPoolDfnMapLockRef;
        storPoolDfnMap = storPoolDfnMapRef;

        lister = new FilteredObjectLister<>(
            "storage pool definition",
            "storage pool",
            new StorPoolHandler()
        );
    }

    @Override
    public void execute(
        PrintStream debugOut,
        PrintStream debugErr,
        Map<String, String> parameters
    )
        throws Exception
    {
        lister.execute(debugOut, debugErr, parameters);
    }

    private class StorPoolHandler implements FilteredObjectLister.ObjectHandler<StorPoolDefinition>
    {
        @Override
        public Lock[] getRequiredLocks()
        {
            return new Lock[] {reconfigurationLock.readLock(), storPoolDfnMapLock.readLock()};
        }

        @Override
        public void ensureSearchAccess()
        {
        }

        @Override
        public Collection<StorPoolDefinition> getAll()
        {
            return storPoolDfnMap.values();
        }

        @Override
        public @Nullable StorPoolDefinition getByName(final String name)
            throws InvalidNameException
        {
            return storPoolDfnMap.get(new StorPoolName(name));
        }

        @Override
        public String getName(final StorPoolDefinition storPoolDfnRef)
        {
            return storPoolDfnRef.getName().value;
        }

        @Override
        public void displayObjects(
            final PrintStream output, final StorPoolDefinition storPoolDfnRef)
        {
            TreePrinter.Builder treeBuilder = TreePrinter.builder(
                "\u001b[1;37m%-40s\u001b[0m %s",
                storPoolDfnRef.getName().displayValue,
                storPoolDfnRef.getUuid().toString().toUpperCase()
            );

            Iterator<StorPool> storPoolIterator = storPoolDfnRef.iterateStorPools();
            while (storPoolIterator.hasNext())
            {
                StorPool storPool = storPoolIterator.next();

                treeBuilder
                    .branch(
                        "\u001b[1;37mStorage pool\u001b[0m %s",
                        storPool.getUuid().toString().toUpperCase()
                    )
                    .leaf("Volatile UUID: %s", UuidUtils.dbgInstanceIdString(storPool))
                    .leaf("Node name: %s", storPool.getNode().getName().displayValue)
                    .leaf("Node UUID: %s", storPool.getNode().getUuid().toString().toUpperCase())
                    .leaf("Node volatile UUID: %s", UuidUtils.dbgInstanceIdString(storPool.getNode()))
                    .leaf("Driver name: %s", storPool.getDeviceProviderKind());

                Collection<VlmProviderObject<Resource>> vlmLayerDataCollection = storPool
                    .getVolumes();
                Collection<VlmProviderObject<Snapshot>> snapVlmLayerDataCollection = storPool
                    .getSnapVolumes();

                treeBuilder.branch("Volumes (count: %d)", vlmLayerDataCollection.size());

                for (VlmProviderObject<Resource> vlmLayerData : vlmLayerDataCollection)
                {
                    Volume vlm = (Volume) vlmLayerData.getVolume();
                    treeBuilder
                        .branch("Volume %s", vlm.getUuid().toString().toUpperCase())
                        .leaf("Flags: %016X", vlm.getFlags().getFlagsBits())
                        .leaf("Volume number: %s", vlm.getVolumeNumber())
                        .endBranch();
                }

                treeBuilder.branch("Snapshots (count: %d)", snapVlmLayerDataCollection.size());

                for (VlmProviderObject<Snapshot> snapVlmData : snapVlmLayerDataCollection)
                {
                    SnapshotVolume snapVlm = (SnapshotVolume) snapVlmData.getVolume();
                    treeBuilder
                        .branch("SnapshotVolume %s", snapVlm.getUuid().toString().toUpperCase())
                        .leaf("Volume number: %s", snapVlm.getVolumeNumber())
                        .endBranch();
                }

                treeBuilder
                    .endBranch()
                    .endBranch()
                    .endBranch();
            }

            treeBuilder.print(output);
        }

        @Override
        public int countObjects(final StorPoolDefinition storPoolDfnRef)
        {
            return 1;
        }
    }
}
