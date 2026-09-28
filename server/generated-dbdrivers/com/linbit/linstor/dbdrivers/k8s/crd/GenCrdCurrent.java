package com.linbit.linstor.dbdrivers.k8s.crd;

import com.linbit.ImplementationError;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.dbdrivers.DatabaseTable;
import com.linbit.linstor.dbdrivers.DatabaseTable.Column;
import com.linbit.linstor.dbdrivers.GeneratedDatabaseTables;
import com.linbit.linstor.dbdrivers.RawParameters;
import com.linbit.linstor.transaction.BaseControllerK8sCrdTransactionMgrContext;
import com.linbit.linstor.transaction.K8sCrdMigrationContext;
import com.linbit.linstor.transaction.K8sCrdSchemaUpdateContext;
import com.linbit.linstor.utils.ByteUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;

@GenCrd(
    dataVersion = GenCrdCurrent.VERSION
)
@SuppressWarnings(
    {
        "checkstyle:LineLength",
        "checkstyle:ReturnCount",
        "checkstyle:TypeName"
    }
)
public class GenCrdCurrent
{
    public static final String VERSION = "v1-34-0";
    public static final String GROUP = "internal.linstor.linbit.com";
    private static final Map<String, String> KEY_LUT = new HashMap<>();
    private static final HashSet<String> USED_K8S_KEYS = new HashSet<>();
    private static final AtomicLong NEXT_ID = new AtomicLong();
    private static final HashMap<String, Class<?>> JSON_ID_TO_TYPE_CLASS_LUT = new HashMap<>();

    static
    {
        JSON_ID_TO_TYPE_CLASS_LUT.put("AuthTokens", AuthTokens.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("AuthTokensSpec", AuthTokensSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("EbsRemotes", EbsRemotes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("EbsRemotesSpec", EbsRemotesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("Files", Files.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("FilesSpec", FilesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("KeyValueStore", KeyValueStore.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("KeyValueStoreSpec", KeyValueStoreSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerBcacheVolumes", LayerBcacheVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerBcacheVolumesSpec", LayerBcacheVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerCacheVolumes", LayerCacheVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerCacheVolumesSpec", LayerCacheVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdResources", LayerDrbdResources.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdResourcesSpec", LayerDrbdResourcesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdResourceDefinitions", LayerDrbdResourceDefinitions.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdResourceDefinitionsSpec", LayerDrbdResourceDefinitionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdVolumes", LayerDrbdVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdVolumesSpec", LayerDrbdVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdVolumeDefinitions", LayerDrbdVolumeDefinitions.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerDrbdVolumeDefinitionsSpec", LayerDrbdVolumeDefinitionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerLuksVolumes", LayerLuksVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerLuksVolumesSpec", LayerLuksVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerResourceIds", LayerResourceIds.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerResourceIdsSpec", LayerResourceIdsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerStorageVolumes", LayerStorageVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerStorageVolumesSpec", LayerStorageVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerWritecacheVolumes", LayerWritecacheVolumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LayerWritecacheVolumesSpec", LayerWritecacheVolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LinstorRemotes", LinstorRemotes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("LinstorRemotesSpec", LinstorRemotesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("Nodes", Nodes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodesSpec", NodesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeConnections", NodeConnections.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeConnectionsSpec", NodeConnectionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeNetInterfaces", NodeNetInterfaces.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeNetInterfacesSpec", NodeNetInterfacesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeStorPool", NodeStorPool.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("NodeStorPoolSpec", NodeStorPoolSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("PropsContainers", PropsContainers.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("PropsContainersSpec", PropsContainersSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("Resources", Resources.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourcesSpec", ResourcesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceConnections", ResourceConnections.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceConnectionsSpec", ResourceConnectionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceDefinitions", ResourceDefinitions.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceDefinitionsSpec", ResourceDefinitionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceGroups", ResourceGroups.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("ResourceGroupsSpec", ResourceGroupsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("S3Remotes", S3Remotes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("S3RemotesSpec", S3RemotesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("SatellitesCapacity", SatellitesCapacity.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("SatellitesCapacitySpec", SatellitesCapacitySpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("Schedules", Schedules.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("SchedulesSpec", SchedulesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("SpaceHistory", SpaceHistory.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("SpaceHistorySpec", SpaceHistorySpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("StorPoolDefinitions", StorPoolDefinitions.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("StorPoolDefinitionsSpec", StorPoolDefinitionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("TrackingDate", TrackingDate.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("TrackingDateSpec", TrackingDateSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("Volumes", Volumes.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumesSpec", VolumesSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeConnections", VolumeConnections.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeConnectionsSpec", VolumeConnectionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeDefinitions", VolumeDefinitions.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeDefinitionsSpec", VolumeDefinitionsSpec.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeGroups", VolumeGroups.class);
        JSON_ID_TO_TYPE_CLASS_LUT.put("VolumeGroupsSpec", VolumeGroupsSpec.class);
    }

    private GenCrdCurrent()
    {
    }

    @SuppressWarnings("unchecked")
    public static <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec<CRD, SPEC>> @Nullable Class<CRD> databaseTableToCustomResourceClass(
        DatabaseTable table
    )
    {
        switch (table.getName())
        {
            case "AUTH_TOKENS":
                return (Class<CRD>) AuthTokens.class;
            case "EBS_REMOTES":
                return (Class<CRD>) EbsRemotes.class;
            case "FILES":
                return (Class<CRD>) Files.class;
            case "KEY_VALUE_STORE":
                return (Class<CRD>) KeyValueStore.class;
            case "LAYER_BCACHE_VOLUMES":
                return (Class<CRD>) LayerBcacheVolumes.class;
            case "LAYER_CACHE_VOLUMES":
                return (Class<CRD>) LayerCacheVolumes.class;
            case "LAYER_DRBD_RESOURCES":
                return (Class<CRD>) LayerDrbdResources.class;
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return (Class<CRD>) LayerDrbdResourceDefinitions.class;
            case "LAYER_DRBD_VOLUMES":
                return (Class<CRD>) LayerDrbdVolumes.class;
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return (Class<CRD>) LayerDrbdVolumeDefinitions.class;
            case "LAYER_LUKS_VOLUMES":
                return (Class<CRD>) LayerLuksVolumes.class;
            case "LAYER_RESOURCE_IDS":
                return (Class<CRD>) LayerResourceIds.class;
            case "LAYER_STORAGE_VOLUMES":
                return (Class<CRD>) LayerStorageVolumes.class;
            case "LAYER_WRITECACHE_VOLUMES":
                return (Class<CRD>) LayerWritecacheVolumes.class;
            case "LINSTOR_REMOTES":
                return (Class<CRD>) LinstorRemotes.class;
            case "NODES":
                return (Class<CRD>) Nodes.class;
            case "NODE_CONNECTIONS":
                return (Class<CRD>) NodeConnections.class;
            case "NODE_NET_INTERFACES":
                return (Class<CRD>) NodeNetInterfaces.class;
            case "NODE_STOR_POOL":
                return (Class<CRD>) NodeStorPool.class;
            case "PROPS_CONTAINERS":
                return (Class<CRD>) PropsContainers.class;
            case "RESOURCES":
                return (Class<CRD>) Resources.class;
            case "RESOURCE_CONNECTIONS":
                return (Class<CRD>) ResourceConnections.class;
            case "RESOURCE_DEFINITIONS":
                return (Class<CRD>) ResourceDefinitions.class;
            case "RESOURCE_GROUPS":
                return (Class<CRD>) ResourceGroups.class;
            case "S3_REMOTES":
                return (Class<CRD>) S3Remotes.class;
            case "SATELLITES_CAPACITY":
                return (Class<CRD>) SatellitesCapacity.class;
            case "SCHEDULES":
                return (Class<CRD>) Schedules.class;
            case "SPACE_HISTORY":
                return (Class<CRD>) SpaceHistory.class;
            case "STOR_POOL_DEFINITIONS":
                return (Class<CRD>) StorPoolDefinitions.class;
            case "TRACKING_DATE":
                return (Class<CRD>) TrackingDate.class;
            case "VOLUMES":
                return (Class<CRD>) Volumes.class;
            case "VOLUME_CONNECTIONS":
                return (Class<CRD>) VolumeConnections.class;
            case "VOLUME_DEFINITIONS":
                return (Class<CRD>) VolumeDefinitions.class;
            case "VOLUME_GROUPS":
                return (Class<CRD>) VolumeGroups.class;
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec<CRD, SPEC>> @Nullable CRD specToCrd(SPEC spec)
    {
        switch (spec.getDatabaseTable().getName())
        {
            case "AUTH_TOKENS":
                return (CRD) new AuthTokens((AuthTokensSpec) spec);
            case "EBS_REMOTES":
                return (CRD) new EbsRemotes((EbsRemotesSpec) spec);
            case "FILES":
                return (CRD) new Files((FilesSpec) spec);
            case "KEY_VALUE_STORE":
                return (CRD) new KeyValueStore((KeyValueStoreSpec) spec);
            case "LAYER_BCACHE_VOLUMES":
                return (CRD) new LayerBcacheVolumes((LayerBcacheVolumesSpec) spec);
            case "LAYER_CACHE_VOLUMES":
                return (CRD) new LayerCacheVolumes((LayerCacheVolumesSpec) spec);
            case "LAYER_DRBD_RESOURCES":
                return (CRD) new LayerDrbdResources((LayerDrbdResourcesSpec) spec);
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return (CRD) new LayerDrbdResourceDefinitions((LayerDrbdResourceDefinitionsSpec) spec);
            case "LAYER_DRBD_VOLUMES":
                return (CRD) new LayerDrbdVolumes((LayerDrbdVolumesSpec) spec);
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return (CRD) new LayerDrbdVolumeDefinitions((LayerDrbdVolumeDefinitionsSpec) spec);
            case "LAYER_LUKS_VOLUMES":
                return (CRD) new LayerLuksVolumes((LayerLuksVolumesSpec) spec);
            case "LAYER_RESOURCE_IDS":
                return (CRD) new LayerResourceIds((LayerResourceIdsSpec) spec);
            case "LAYER_STORAGE_VOLUMES":
                return (CRD) new LayerStorageVolumes((LayerStorageVolumesSpec) spec);
            case "LAYER_WRITECACHE_VOLUMES":
                return (CRD) new LayerWritecacheVolumes((LayerWritecacheVolumesSpec) spec);
            case "LINSTOR_REMOTES":
                return (CRD) new LinstorRemotes((LinstorRemotesSpec) spec);
            case "NODES":
                return (CRD) new Nodes((NodesSpec) spec);
            case "NODE_CONNECTIONS":
                return (CRD) new NodeConnections((NodeConnectionsSpec) spec);
            case "NODE_NET_INTERFACES":
                return (CRD) new NodeNetInterfaces((NodeNetInterfacesSpec) spec);
            case "NODE_STOR_POOL":
                return (CRD) new NodeStorPool((NodeStorPoolSpec) spec);
            case "PROPS_CONTAINERS":
                return (CRD) new PropsContainers((PropsContainersSpec) spec);
            case "RESOURCES":
                return (CRD) new Resources((ResourcesSpec) spec);
            case "RESOURCE_CONNECTIONS":
                return (CRD) new ResourceConnections((ResourceConnectionsSpec) spec);
            case "RESOURCE_DEFINITIONS":
                return (CRD) new ResourceDefinitions((ResourceDefinitionsSpec) spec);
            case "RESOURCE_GROUPS":
                return (CRD) new ResourceGroups((ResourceGroupsSpec) spec);
            case "S3_REMOTES":
                return (CRD) new S3Remotes((S3RemotesSpec) spec);
            case "SATELLITES_CAPACITY":
                return (CRD) new SatellitesCapacity((SatellitesCapacitySpec) spec);
            case "SCHEDULES":
                return (CRD) new Schedules((SchedulesSpec) spec);
            case "SPACE_HISTORY":
                return (CRD) new SpaceHistory((SpaceHistorySpec) spec);
            case "STOR_POOL_DEFINITIONS":
                return (CRD) new StorPoolDefinitions((StorPoolDefinitionsSpec) spec);
            case "TRACKING_DATE":
                return (CRD) new TrackingDate((TrackingDateSpec) spec);
            case "VOLUMES":
                return (CRD) new Volumes((VolumesSpec) spec);
            case "VOLUME_CONNECTIONS":
                return (CRD) new VolumeConnections((VolumeConnectionsSpec) spec);
            case "VOLUME_DEFINITIONS":
                return (CRD) new VolumeDefinitions((VolumeDefinitionsSpec) spec);
            case "VOLUME_GROUPS":
                return (CRD) new VolumeGroups((VolumeGroupsSpec) spec);
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec<CRD, SPEC>> @Nullable Class<SPEC> databaseTableToSpecClass(
        DatabaseTable table
    )
    {
        switch (table.getName())
        {
            case "AUTH_TOKENS":
                return (Class<SPEC>) AuthTokensSpec.class;
            case "EBS_REMOTES":
                return (Class<SPEC>) EbsRemotesSpec.class;
            case "FILES":
                return (Class<SPEC>) FilesSpec.class;
            case "KEY_VALUE_STORE":
                return (Class<SPEC>) KeyValueStoreSpec.class;
            case "LAYER_BCACHE_VOLUMES":
                return (Class<SPEC>) LayerBcacheVolumesSpec.class;
            case "LAYER_CACHE_VOLUMES":
                return (Class<SPEC>) LayerCacheVolumesSpec.class;
            case "LAYER_DRBD_RESOURCES":
                return (Class<SPEC>) LayerDrbdResourcesSpec.class;
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return (Class<SPEC>) LayerDrbdResourceDefinitionsSpec.class;
            case "LAYER_DRBD_VOLUMES":
                return (Class<SPEC>) LayerDrbdVolumesSpec.class;
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return (Class<SPEC>) LayerDrbdVolumeDefinitionsSpec.class;
            case "LAYER_LUKS_VOLUMES":
                return (Class<SPEC>) LayerLuksVolumesSpec.class;
            case "LAYER_RESOURCE_IDS":
                return (Class<SPEC>) LayerResourceIdsSpec.class;
            case "LAYER_STORAGE_VOLUMES":
                return (Class<SPEC>) LayerStorageVolumesSpec.class;
            case "LAYER_WRITECACHE_VOLUMES":
                return (Class<SPEC>) LayerWritecacheVolumesSpec.class;
            case "LINSTOR_REMOTES":
                return (Class<SPEC>) LinstorRemotesSpec.class;
            case "NODES":
                return (Class<SPEC>) NodesSpec.class;
            case "NODE_CONNECTIONS":
                return (Class<SPEC>) NodeConnectionsSpec.class;
            case "NODE_NET_INTERFACES":
                return (Class<SPEC>) NodeNetInterfacesSpec.class;
            case "NODE_STOR_POOL":
                return (Class<SPEC>) NodeStorPoolSpec.class;
            case "PROPS_CONTAINERS":
                return (Class<SPEC>) PropsContainersSpec.class;
            case "RESOURCES":
                return (Class<SPEC>) ResourcesSpec.class;
            case "RESOURCE_CONNECTIONS":
                return (Class<SPEC>) ResourceConnectionsSpec.class;
            case "RESOURCE_DEFINITIONS":
                return (Class<SPEC>) ResourceDefinitionsSpec.class;
            case "RESOURCE_GROUPS":
                return (Class<SPEC>) ResourceGroupsSpec.class;
            case "S3_REMOTES":
                return (Class<SPEC>) S3RemotesSpec.class;
            case "SATELLITES_CAPACITY":
                return (Class<SPEC>) SatellitesCapacitySpec.class;
            case "SCHEDULES":
                return (Class<SPEC>) SchedulesSpec.class;
            case "SPACE_HISTORY":
                return (Class<SPEC>) SpaceHistorySpec.class;
            case "STOR_POOL_DEFINITIONS":
                return (Class<SPEC>) StorPoolDefinitionsSpec.class;
            case "TRACKING_DATE":
                return (Class<SPEC>) TrackingDateSpec.class;
            case "VOLUMES":
                return (Class<SPEC>) VolumesSpec.class;
            case "VOLUME_CONNECTIONS":
                return (Class<SPEC>) VolumeConnectionsSpec.class;
            case "VOLUME_DEFINITIONS":
                return (Class<SPEC>) VolumeDefinitionsSpec.class;
            case "VOLUME_GROUPS":
                return (Class<SPEC>) VolumeGroupsSpec.class;
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec<CRD, SPEC>> @Nullable SPEC rawParamToSpec(
        DatabaseTable tableRef,
        RawParameters rawDataMapRef
    )
    {
        switch (tableRef.getName())
        {
            case "AUTH_TOKENS":
                return (SPEC) AuthTokensSpec.fromRawParameters(rawDataMapRef);
            case "EBS_REMOTES":
                return (SPEC) EbsRemotesSpec.fromRawParameters(rawDataMapRef);
            case "FILES":
                return (SPEC) FilesSpec.fromRawParameters(rawDataMapRef);
            case "KEY_VALUE_STORE":
                return (SPEC) KeyValueStoreSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_BCACHE_VOLUMES":
                return (SPEC) LayerBcacheVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_CACHE_VOLUMES":
                return (SPEC) LayerCacheVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_DRBD_RESOURCES":
                return (SPEC) LayerDrbdResourcesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return (SPEC) LayerDrbdResourceDefinitionsSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_DRBD_VOLUMES":
                return (SPEC) LayerDrbdVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return (SPEC) LayerDrbdVolumeDefinitionsSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_LUKS_VOLUMES":
                return (SPEC) LayerLuksVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_RESOURCE_IDS":
                return (SPEC) LayerResourceIdsSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_STORAGE_VOLUMES":
                return (SPEC) LayerStorageVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LAYER_WRITECACHE_VOLUMES":
                return (SPEC) LayerWritecacheVolumesSpec.fromRawParameters(rawDataMapRef);
            case "LINSTOR_REMOTES":
                return (SPEC) LinstorRemotesSpec.fromRawParameters(rawDataMapRef);
            case "NODES":
                return (SPEC) NodesSpec.fromRawParameters(rawDataMapRef);
            case "NODE_CONNECTIONS":
                return (SPEC) NodeConnectionsSpec.fromRawParameters(rawDataMapRef);
            case "NODE_NET_INTERFACES":
                return (SPEC) NodeNetInterfacesSpec.fromRawParameters(rawDataMapRef);
            case "NODE_STOR_POOL":
                return (SPEC) NodeStorPoolSpec.fromRawParameters(rawDataMapRef);
            case "PROPS_CONTAINERS":
                return (SPEC) PropsContainersSpec.fromRawParameters(rawDataMapRef);
            case "RESOURCES":
                return (SPEC) ResourcesSpec.fromRawParameters(rawDataMapRef);
            case "RESOURCE_CONNECTIONS":
                return (SPEC) ResourceConnectionsSpec.fromRawParameters(rawDataMapRef);
            case "RESOURCE_DEFINITIONS":
                return (SPEC) ResourceDefinitionsSpec.fromRawParameters(rawDataMapRef);
            case "RESOURCE_GROUPS":
                return (SPEC) ResourceGroupsSpec.fromRawParameters(rawDataMapRef);
            case "S3_REMOTES":
                return (SPEC) S3RemotesSpec.fromRawParameters(rawDataMapRef);
            case "SATELLITES_CAPACITY":
                return (SPEC) SatellitesCapacitySpec.fromRawParameters(rawDataMapRef);
            case "SCHEDULES":
                return (SPEC) SchedulesSpec.fromRawParameters(rawDataMapRef);
            case "SPACE_HISTORY":
                return (SPEC) SpaceHistorySpec.fromRawParameters(rawDataMapRef);
            case "STOR_POOL_DEFINITIONS":
                return (SPEC) StorPoolDefinitionsSpec.fromRawParameters(rawDataMapRef);
            case "TRACKING_DATE":
                return (SPEC) TrackingDateSpec.fromRawParameters(rawDataMapRef);
            case "VOLUMES":
                return (SPEC) VolumesSpec.fromRawParameters(rawDataMapRef);
            case "VOLUME_CONNECTIONS":
                return (SPEC) VolumeConnectionsSpec.fromRawParameters(rawDataMapRef);
            case "VOLUME_DEFINITIONS":
                return (SPEC) VolumeDefinitionsSpec.fromRawParameters(rawDataMapRef);
            case "VOLUME_GROUPS":
                return (SPEC) VolumeGroupsSpec.fromRawParameters(rawDataMapRef);
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <DATA, CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec<CRD, SPEC>> @Nullable CRD dataToCrd(
        DatabaseTable table,
        Map<Column, Function<DATA, Object>> setters,
        DATA data
    )
    {
        switch (table.getName())
        {
            case "AUTH_TOKENS":
            {
                return (CRD) new AuthTokensSpec(
                    (int) setters.get(GeneratedDatabaseTables.AuthTokens.ID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.AuthTokens.TOKEN_HASH).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.AuthTokens.DESCRIPTION).apply(data),
                    (boolean) setters.get(GeneratedDatabaseTables.AuthTokens.IS_ACTIVE).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.AuthTokens.CREATED_AT).apply(data),
                    (Long) setters.get(GeneratedDatabaseTables.AuthTokens.DELETED_AT).apply(data),
                    (Long) setters.get(GeneratedDatabaseTables.AuthTokens.EXPIRES_AT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.AuthTokens.IP_FILTER).apply(data),
                    (boolean) setters.get(GeneratedDatabaseTables.AuthTokens.IS_USER_TOKEN).apply(data)
                ).getCrd();
            }
            case "EBS_REMOTES":
            {
                return (CRD) new EbsRemotesSpec(
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.EbsRemotes.FLAGS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.URL).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.REGION).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.EbsRemotes.AVAILABILITY_ZONE).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.EbsRemotes.ACCESS_KEY).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.EbsRemotes.SECRET_KEY).apply(data)
                ).getCrd();
            }
            case "FILES":
            {
                return (CRD) new FilesSpec(
                    (String) setters.get(GeneratedDatabaseTables.Files.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Files.PATH).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Files.FLAGS).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.Files.CONTENT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Files.CONTENT_CHECKSUM).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.Files.ALT_SUFFIXES).apply(data)
                ).getCrd();
            }
            case "KEY_VALUE_STORE":
            {
                return (CRD) new KeyValueStoreSpec(
                    (String) setters.get(GeneratedDatabaseTables.KeyValueStore.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.KeyValueStore.KVS_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.KeyValueStore.KVS_DSP_NAME).apply(data)
                ).getCrd();
            }
            case "LAYER_BCACHE_VOLUMES":
            {
                return (CRD) new LayerBcacheVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerBcacheVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerBcacheVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerBcacheVolumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerBcacheVolumes.POOL_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerBcacheVolumes.DEV_UUID).apply(data)
                ).getCrd();
            }
            case "LAYER_CACHE_VOLUMES":
            {
                return (CRD) new LayerCacheVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerCacheVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerCacheVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerCacheVolumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerCacheVolumes.POOL_NAME_CACHE).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerCacheVolumes.POOL_NAME_META).apply(data)
                ).getCrd();
            }
            case "LAYER_DRBD_RESOURCES":
            {
                return (CRD) new LayerDrbdResourcesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResources.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResources.PEER_SLOTS).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResources.AL_STRIPES).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.LayerDrbdResources.AL_STRIPE_SIZE).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.LayerDrbdResources.FLAGS).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResources.NODE_ID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResources.TCP_PORT_LIST).apply(data)
                ).getCrd();
            }
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
            {
                return (CRD) new LayerDrbdResourceDefinitionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.RESOURCE_NAME_SUFFIX).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.SNAPSHOT_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.PEER_SLOTS).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.AL_STRIPES).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.AL_STRIPE_SIZE).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.TCP_PORT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.TRANSPORT_TYPE).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.SECRET).apply(data)
                ).getCrd();
            }
            case "LAYER_DRBD_VOLUMES":
            {
                return (CRD) new LayerDrbdVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdVolumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdVolumes.POOL_NAME).apply(data)
                ).getCrd();
            }
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
            {
                return (CRD) new LayerDrbdVolumeDefinitionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.RESOURCE_NAME_SUFFIX).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.SNAPSHOT_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.VLM_NR).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.VLM_MINOR_NR).apply(data)
                ).getCrd();
            }
            case "LAYER_LUKS_VOLUMES":
            {
                return (CRD) new LayerLuksVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerLuksVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerLuksVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerLuksVolumes.ENCRYPTED_PASSWORD).apply(data)
                ).getCrd();
            }
            case "LAYER_RESOURCE_IDS":
            {
                return (CRD) new LayerResourceIdsSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_ID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerResourceIds.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerResourceIds.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerResourceIds.SNAPSHOT_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_KIND).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_PARENT_ID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_SUFFIX).apply(data),
                    (boolean) setters.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_SUSPENDED).apply(data)
                ).getCrd();
            }
            case "LAYER_STORAGE_VOLUMES":
            {
                return (CRD) new LayerStorageVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerStorageVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerStorageVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerStorageVolumes.PROVIDER_KIND).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerStorageVolumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerStorageVolumes.STOR_POOL_NAME).apply(data)
                ).getCrd();
            }
            case "LAYER_WRITECACHE_VOLUMES":
            {
                return (CRD) new LayerWritecacheVolumesSpec(
                    (int) setters.get(GeneratedDatabaseTables.LayerWritecacheVolumes.LAYER_RESOURCE_ID).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.LayerWritecacheVolumes.VLM_NR).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerWritecacheVolumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LayerWritecacheVolumes.POOL_NAME).apply(data)
                ).getCrd();
            }
            case "LINSTOR_REMOTES":
            {
                return (CRD) new LinstorRemotesSpec(
                    (String) setters.get(GeneratedDatabaseTables.LinstorRemotes.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LinstorRemotes.NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LinstorRemotes.DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.LinstorRemotes.FLAGS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LinstorRemotes.URL).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.LinstorRemotes.ENCRYPTED_PASSPHRASE).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.LinstorRemotes.CLUSTER_ID).apply(data)
                ).getCrd();
            }
            case "NODES":
            {
                return (CRD) new NodesSpec(
                    (String) setters.get(GeneratedDatabaseTables.Nodes.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Nodes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Nodes.NODE_DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Nodes.NODE_FLAGS).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.Nodes.NODE_TYPE).apply(data)
                ).getCrd();
            }
            case "NODE_CONNECTIONS":
            {
                return (CRD) new NodeConnectionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.NodeConnections.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeConnections.NODE_NAME_SRC).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeConnections.NODE_NAME_DST).apply(data)
                ).getCrd();
            }
            case "NODE_NET_INTERFACES":
            {
                return (CRD) new NodeNetInterfacesSpec(
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NET_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NET_DSP_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.INET_ADDRESS).apply(data),
                    (Short) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.STLT_CONN_PORT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeNetInterfaces.STLT_CONN_ENCR_TYPE).apply(data)
                ).getCrd();
            }
            case "NODE_STOR_POOL":
            {
                return (CRD) new NodeStorPoolSpec(
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.POOL_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.DRIVER_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.FREE_SPACE_MGR_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.NodeStorPool.FREE_SPACE_MGR_DSP_NAME).apply(data),
                    (boolean) setters.get(GeneratedDatabaseTables.NodeStorPool.EXTERNAL_LOCKING).apply(data)
                ).getCrd();
            }
            case "PROPS_CONTAINERS":
            {
                return (CRD) new PropsContainersSpec(
                    (String) setters.get(GeneratedDatabaseTables.PropsContainers.PROPS_INSTANCE).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.PropsContainers.PROP_KEY).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.PropsContainers.PROP_VALUE).apply(data)
                ).getCrd();
            }
            case "RESOURCES":
            {
                return (CRD) new ResourcesSpec(
                    (String) setters.get(GeneratedDatabaseTables.Resources.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Resources.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Resources.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Resources.SNAPSHOT_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Resources.RESOURCE_FLAGS).apply(data),
                    (Long) setters.get(GeneratedDatabaseTables.Resources.CREATE_TIMESTAMP).apply(data)
                ).getCrd();
            }
            case "RESOURCE_CONNECTIONS":
            {
                return (CRD) new ResourceConnectionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.ResourceConnections.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceConnections.NODE_NAME_SRC).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceConnections.NODE_NAME_DST).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceConnections.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceConnections.SNAPSHOT_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.ResourceConnections.FLAGS).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.ResourceConnections.TCP_PORT_SRC).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.ResourceConnections.TCP_PORT_DST).apply(data)
                ).getCrd();
            }
            case "RESOURCE_DEFINITIONS":
            {
                return (CRD) new ResourceDefinitionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.SNAPSHOT_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_DSP_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.SNAPSHOT_DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_FLAGS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.LAYER_STACK).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_EXTERNAL_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_GROUP_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceDefinitions.PARENT_UUID).apply(data)
                ).getCrd();
            }
            case "RESOURCE_GROUPS":
            {
                return (CRD) new ResourceGroupsSpec(
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.RESOURCE_GROUP_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.RESOURCE_GROUP_DSP_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.DESCRIPTION).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.LAYER_STACK).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.ResourceGroups.REPLICA_COUNT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.NODE_NAME_LIST).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.POOL_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.POOL_NAME_DISKLESS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.DO_NOT_PLACE_WITH_RSC_REGEX).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.DO_NOT_PLACE_WITH_RSC_LIST).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.REPLICAS_ON_SAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.REPLICAS_ON_DIFFERENT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.X_REPLICAS_ON_DIFFERENT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.ResourceGroups.ALLOWED_PROVIDER_LIST).apply(data),
                    (Boolean) setters.get(GeneratedDatabaseTables.ResourceGroups.DISKLESS_ON_REMAINING).apply(data),
                    (Short) setters.get(GeneratedDatabaseTables.ResourceGroups.PEER_SLOTS).apply(data)
                ).getCrd();
            }
            case "S3_REMOTES":
            {
                return (CRD) new S3RemotesSpec(
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.S3Remotes.FLAGS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.ENDPOINT).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.BUCKET).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.S3Remotes.REGION).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.S3Remotes.ACCESS_KEY).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.S3Remotes.SECRET_KEY).apply(data)
                ).getCrd();
            }
            case "SATELLITES_CAPACITY":
            {
                return (CRD) new SatellitesCapacitySpec(
                    (String) setters.get(GeneratedDatabaseTables.SatellitesCapacity.NODE_NAME).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.SatellitesCapacity.CAPACITY).apply(data),
                    (boolean) setters.get(GeneratedDatabaseTables.SatellitesCapacity.FAIL_FLAG).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.SatellitesCapacity.ALLOCATED).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.SatellitesCapacity.USABLE).apply(data)
                ).getCrd();
            }
            case "SCHEDULES":
            {
                return (CRD) new SchedulesSpec(
                    (String) setters.get(GeneratedDatabaseTables.Schedules.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Schedules.NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Schedules.DSP_NAME).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Schedules.FLAGS).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Schedules.FULL_CRON).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Schedules.INC_CRON).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.Schedules.KEEP_LOCAL).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.Schedules.KEEP_REMOTE).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Schedules.ON_FAILURE).apply(data),
                    (Integer) setters.get(GeneratedDatabaseTables.Schedules.MAX_RETRIES).apply(data)
                ).getCrd();
            }
            case "SPACE_HISTORY":
            {
                return (CRD) new SpaceHistorySpec(
                    (long) setters.get(GeneratedDatabaseTables.SpaceHistory.ENTRY_DATE).apply(data),
                    (byte[]) setters.get(GeneratedDatabaseTables.SpaceHistory.CAPACITY).apply(data)
                ).getCrd();
            }
            case "STOR_POOL_DEFINITIONS":
            {
                return (CRD) new StorPoolDefinitionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.StorPoolDefinitions.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.StorPoolDefinitions.POOL_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.StorPoolDefinitions.POOL_DSP_NAME).apply(data)
                ).getCrd();
            }
            case "TRACKING_DATE":
            {
                return (CRD) new TrackingDateSpec(
                    (long) setters.get(GeneratedDatabaseTables.TrackingDate.ENTRY_DATE).apply(data)
                ).getCrd();
            }
            case "VOLUMES":
            {
                return (CRD) new VolumesSpec(
                    (String) setters.get(GeneratedDatabaseTables.Volumes.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Volumes.NODE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Volumes.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.Volumes.SNAPSHOT_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.Volumes.VLM_NR).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.Volumes.VLM_FLAGS).apply(data)
                ).getCrd();
            }
            case "VOLUME_CONNECTIONS":
            {
                return (CRD) new VolumeConnectionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.VolumeConnections.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeConnections.NODE_NAME_SRC).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeConnections.NODE_NAME_DST).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeConnections.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeConnections.SNAPSHOT_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.VolumeConnections.VLM_NR).apply(data)
                ).getCrd();
            }
            case "VOLUME_DEFINITIONS":
            {
                return (CRD) new VolumeDefinitionsSpec(
                    (String) setters.get(GeneratedDatabaseTables.VolumeDefinitions.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeDefinitions.RESOURCE_NAME).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeDefinitions.SNAPSHOT_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_NR).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_SIZE).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_FLAGS).apply(data)
                ).getCrd();
            }
            case "VOLUME_GROUPS":
            {
                return (CRD) new VolumeGroupsSpec(
                    (String) setters.get(GeneratedDatabaseTables.VolumeGroups.UUID).apply(data),
                    (String) setters.get(GeneratedDatabaseTables.VolumeGroups.RESOURCE_GROUP_NAME).apply(data),
                    (int) setters.get(GeneratedDatabaseTables.VolumeGroups.VLM_NR).apply(data),
                    (long) setters.get(GeneratedDatabaseTables.VolumeGroups.FLAGS).apply(data)
                ).getCrd();
            }
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    public static @Nullable String databaseTableToYamlLocation(DatabaseTable dbTable)
    {
        switch (dbTable.getName())
        {
            case "AUTH_TOKENS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/AuthTokens.yaml";
            case "EBS_REMOTES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/EbsRemotes.yaml";
            case "FILES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/Files.yaml";
            case "KEY_VALUE_STORE":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/KeyValueStore.yaml";
            case "LAYER_BCACHE_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerBcacheVolumes.yaml";
            case "LAYER_CACHE_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerCacheVolumes.yaml";
            case "LAYER_DRBD_RESOURCES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerDrbdResources.yaml";
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerDrbdResourceDefinitions.yaml";
            case "LAYER_DRBD_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerDrbdVolumes.yaml";
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerDrbdVolumeDefinitions.yaml";
            case "LAYER_LUKS_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerLuksVolumes.yaml";
            case "LAYER_RESOURCE_IDS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerResourceIds.yaml";
            case "LAYER_STORAGE_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerStorageVolumes.yaml";
            case "LAYER_WRITECACHE_VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LayerWritecacheVolumes.yaml";
            case "LINSTOR_REMOTES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/LinstorRemotes.yaml";
            case "NODES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/Nodes.yaml";
            case "NODE_CONNECTIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/NodeConnections.yaml";
            case "NODE_NET_INTERFACES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/NodeNetInterfaces.yaml";
            case "NODE_STOR_POOL":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/NodeStorPool.yaml";
            case "PROPS_CONTAINERS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/PropsContainers.yaml";
            case "RESOURCES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/Resources.yaml";
            case "RESOURCE_CONNECTIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/ResourceConnections.yaml";
            case "RESOURCE_DEFINITIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/ResourceDefinitions.yaml";
            case "RESOURCE_GROUPS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/ResourceGroups.yaml";
            case "S3_REMOTES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/S3Remotes.yaml";
            case "SATELLITES_CAPACITY":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/SatellitesCapacity.yaml";
            case "SCHEDULES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/Schedules.yaml";
            case "SPACE_HISTORY":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/SpaceHistory.yaml";
            case "STOR_POOL_DEFINITIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/StorPoolDefinitions.yaml";
            case "TRACKING_DATE":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/TrackingDate.yaml";
            case "VOLUMES":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/Volumes.yaml";
            case "VOLUME_CONNECTIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/VolumeConnections.yaml";
            case "VOLUME_DEFINITIONS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/VolumeDefinitions.yaml";
            case "VOLUME_GROUPS":
                return "/com/linbit/linstor/dbcp/k8s/crd/v1_34_0/VolumeGroups.yaml";
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    public static @Nullable String databaseTableToYamlName(DatabaseTable dbTable)
    {
        switch (dbTable.getName())
        {
            case "AUTH_TOKENS":
                return "authtokens";
            case "EBS_REMOTES":
                return "ebsremotes";
            case "FILES":
                return "files";
            case "KEY_VALUE_STORE":
                return "keyvaluestore";
            case "LAYER_BCACHE_VOLUMES":
                return "layerbcachevolumes";
            case "LAYER_CACHE_VOLUMES":
                return "layercachevolumes";
            case "LAYER_DRBD_RESOURCES":
                return "layerdrbdresources";
            case "LAYER_DRBD_RESOURCE_DEFINITIONS":
                return "layerdrbdresourcedefinitions";
            case "LAYER_DRBD_VOLUMES":
                return "layerdrbdvolumes";
            case "LAYER_DRBD_VOLUME_DEFINITIONS":
                return "layerdrbdvolumedefinitions";
            case "LAYER_LUKS_VOLUMES":
                return "layerluksvolumes";
            case "LAYER_RESOURCE_IDS":
                return "layerresourceids";
            case "LAYER_STORAGE_VOLUMES":
                return "layerstoragevolumes";
            case "LAYER_WRITECACHE_VOLUMES":
                return "layerwritecachevolumes";
            case "LINSTOR_REMOTES":
                return "linstorremotes";
            case "NODES":
                return "nodes";
            case "NODE_CONNECTIONS":
                return "nodeconnections";
            case "NODE_NET_INTERFACES":
                return "nodenetinterfaces";
            case "NODE_STOR_POOL":
                return "nodestorpool";
            case "PROPS_CONTAINERS":
                return "propscontainers";
            case "RESOURCES":
                return "resources";
            case "RESOURCE_CONNECTIONS":
                return "resourceconnections";
            case "RESOURCE_DEFINITIONS":
                return "resourcedefinitions";
            case "RESOURCE_GROUPS":
                return "resourcegroups";
            case "S3_REMOTES":
                return "s3remotes";
            case "SATELLITES_CAPACITY":
                return "satellitescapacity";
            case "SCHEDULES":
                return "schedules";
            case "SPACE_HISTORY":
                return "spacehistory";
            case "STOR_POOL_DEFINITIONS":
                return "storpooldefinitions";
            case "TRACKING_DATE":
                return "trackingdate";
            case "VOLUMES":
                return "volumes";
            case "VOLUME_CONNECTIONS":
                return "volumeconnections";
            case "VOLUME_DEFINITIONS":
                return "volumedefinitions";
            case "VOLUME_GROUPS":
                return "volumegroups";
            default:
                // we are most likely iterating tables the current version does not know about.
                return null;
        }
    }

    public static BaseControllerK8sCrdTransactionMgrContext createTxMgrContext()
    {
        return new BaseControllerK8sCrdTransactionMgrContext(
            GenCrdCurrent::databaseTableToCustomResourceClass,
            GeneratedDatabaseTables.ALL_TABLES,
            GenCrdCurrent.VERSION
        );
    }

    public static K8sCrdSchemaUpdateContext createSchemaUpdateContext()
    {
        return new K8sCrdSchemaUpdateContext(
            GenCrdCurrent::databaseTableToYamlLocation,
            GenCrdCurrent::databaseTableToYamlName,
            GenCrdCurrent.VERSION
        );
    }

    public static K8sCrdMigrationContext createMigrationContext()
    {
        return new K8sCrdMigrationContext(createTxMgrContext(), createSchemaUpdateContext());
    }

    public static AuthTokens createAuthTokens(
        int id,
        String tokenHash,
        String description,
        boolean isActive,
        long createdAt,
        Long deletedAt,
        Long expiresAt,
        String ipFilter,
        boolean isUserToken
    )
    {
        return new AuthTokens(
            new AuthTokensSpec(
                id,
                tokenHash,
                description,
                isActive,
                createdAt,
                deletedAt,
                expiresAt,
                ipFilter,
                isUserToken
            )
        );
    }

    @LinstorData(
        tableName = "AUTH_TOKENS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class AuthTokensSpec implements LinstorSpec<AuthTokens, AuthTokensSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5563879569162505403L;
        @JsonIgnore private static final String PK_FORMAT = "%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable AuthTokens parentCrd;

        @JsonProperty("id") public final int id; // PK
        @JsonProperty("token_hash") public final String tokenHash;
        @JsonProperty("description") public final String description;
        @JsonProperty("is_active") public final boolean isActive;
        @JsonProperty("created_at") public final long createdAt;
        @JsonProperty("deleted_at") public final Long deletedAt;
        @JsonProperty("expires_at") public final Long expiresAt;
        @JsonProperty("ip_filter") public final String ipFilter;
        @JsonProperty("is_user_token") public final boolean isUserToken;

        @JsonIgnore
        public static AuthTokensSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new AuthTokensSpec(
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.ID),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.TOKEN_HASH),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.DESCRIPTION),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.IS_ACTIVE),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.CREATED_AT),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.DELETED_AT),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.EXPIRES_AT),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.IP_FILTER),
                rawParamsRef.get(GeneratedDatabaseTables.AuthTokens.IS_USER_TOKEN)
            );
        }

        @JsonCreator
        public AuthTokensSpec(
            @JsonProperty("id") int idRef,
            @JsonProperty("token_hash") String tokenHashRef,
            @JsonProperty("description") String descriptionRef,
            @JsonProperty("is_active") boolean isActiveRef,
            @JsonProperty("created_at") long createdAtRef,
            @JsonProperty("deleted_at") Long deletedAtRef,
            @JsonProperty("expires_at") Long expiresAtRef,
            @JsonProperty("ip_filter") String ipFilterRef,
            @JsonProperty("is_user_token") boolean isUserTokenRef
        )
        {
            id = idRef;
            tokenHash = tokenHashRef;
            description = descriptionRef;
            isActive = isActiveRef;
            createdAt = createdAtRef;
            deletedAt = deletedAtRef;
            expiresAt = expiresAtRef;
            ipFilter = ipFilterRef;
            isUserToken = isUserTokenRef;

            formattedPrimaryKey = String.format(
                AuthTokensSpec.PK_FORMAT,
                id
            );
        }

        @JsonIgnore
        @Override
        public AuthTokens getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new AuthTokens(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("ID", id);
            ret.put("TOKEN_HASH", tokenHash);
            ret.put("DESCRIPTION", description);
            ret.put("IS_ACTIVE", isActive);
            ret.put("CREATED_AT", createdAt);
            ret.put("DELETED_AT", deletedAt);
            ret.put("EXPIRES_AT", expiresAt);
            ret.put("IP_FILTER", ipFilter);
            ret.put("IS_USER_TOKEN", isUserToken);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "ID":
                    return id;
                case "TOKEN_HASH":
                    return tokenHash;
                case "DESCRIPTION":
                    return description;
                case "IS_ACTIVE":
                    return isActive;
                case "CREATED_AT":
                    return createdAt;
                case "DELETED_AT":
                    return deletedAt;
                case "EXPIRES_AT":
                    return expiresAt;
                case "IP_FILTER":
                    return ipFilter;
                case "IS_USER_TOKEN":
                    return isUserToken;
                default:
                    throw new ImplementationError("Unknown database column. Table: AUTH_TOKENS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.AUTH_TOKENS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("authtokens")
    @Singular("authtokens")
    public static class AuthTokens extends CustomResource<AuthTokensSpec, Void> implements LinstorCrd<AuthTokensSpec>
    {
        private static final long serialVersionUID = -1267056226836507464L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public AuthTokens()
        {
        }

        public AuthTokens(AuthTokensSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(AuthTokensSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static EbsRemotes createEbsRemotes(
        String uuid,
        String name,
        String dspName,
        long flags,
        String url,
        String region,
        String availabilityZone,
        byte[] accessKey,
        byte[] secretKey
    )
    {
        return new EbsRemotes(
            new EbsRemotesSpec(
                uuid,
                name,
                dspName,
                flags,
                url,
                region,
                availabilityZone,
                accessKey,
                secretKey
            )
        );
    }

    @LinstorData(
        tableName = "EBS_REMOTES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class EbsRemotesSpec implements LinstorSpec<EbsRemotes, EbsRemotesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 3015966064782723402L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable EbsRemotes parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("name") public final String name; // PK
        @JsonProperty("dsp_name") public final String dspName;
        @JsonProperty("flags") public final long flags;
        @JsonProperty("url") public final String url;
        @JsonProperty("region") public final String region;
        @JsonProperty("availability_zone") public final String availabilityZone;
        @JsonProperty("access_key") public final byte[] accessKey;
        @JsonProperty("secret_key") public final byte[] secretKey;

        @JsonIgnore
        public static EbsRemotesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new EbsRemotesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.NAME),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.URL),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.REGION),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.AVAILABILITY_ZONE),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.ACCESS_KEY),
                rawParamsRef.get(GeneratedDatabaseTables.EbsRemotes.SECRET_KEY)
            );
        }

        @JsonCreator
        public EbsRemotesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("name") String nameRef,
            @JsonProperty("dsp_name") String dspNameRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("url") String urlRef,
            @JsonProperty("region") String regionRef,
            @JsonProperty("availability_zone") String availabilityZoneRef,
            @JsonProperty("access_key") byte[] accessKeyRef,
            @JsonProperty("secret_key") byte[] secretKeyRef
        )
        {
            uuid = uuidRef;
            name = nameRef;
            dspName = dspNameRef;
            flags = flagsRef;
            url = urlRef;
            region = regionRef;
            availabilityZone = availabilityZoneRef;
            accessKey = accessKeyRef;
            secretKey = secretKeyRef;

            formattedPrimaryKey = String.format(
                EbsRemotesSpec.PK_FORMAT,
                name
            );
        }

        @JsonIgnore
        @Override
        public EbsRemotes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new EbsRemotes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NAME", name);
            ret.put("DSP_NAME", dspName);
            ret.put("FLAGS", flags);
            ret.put("URL", url);
            ret.put("REGION", region);
            ret.put("AVAILABILITY_ZONE", availabilityZone);
            ret.put("ACCESS_KEY", accessKey);
            ret.put("SECRET_KEY", secretKey);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NAME":
                    return name;
                case "DSP_NAME":
                    return dspName;
                case "FLAGS":
                    return flags;
                case "URL":
                    return url;
                case "REGION":
                    return region;
                case "AVAILABILITY_ZONE":
                    return availabilityZone;
                case "ACCESS_KEY":
                    return accessKey;
                case "SECRET_KEY":
                    return secretKey;
                default:
                    throw new ImplementationError("Unknown database column. Table: EBS_REMOTES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.EBS_REMOTES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("ebsremotes")
    @Singular("ebsremotes")
    public static class EbsRemotes extends CustomResource<EbsRemotesSpec, Void> implements LinstorCrd<EbsRemotesSpec>
    {
        private static final long serialVersionUID = 256802756295696970L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public EbsRemotes()
        {
        }

        public EbsRemotes(EbsRemotesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(EbsRemotesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static Files createFiles(
        String uuid,
        String path,
        long flags,
        byte[] content,
        String contentChecksum,
        byte[] altSuffixes
    )
    {
        return new Files(
            new FilesSpec(
                uuid,
                path,
                flags,
                content,
                contentChecksum,
                altSuffixes
            )
        );
    }

    @LinstorData(
        tableName = "FILES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class FilesSpec implements LinstorSpec<Files, FilesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -3786219631568292568L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable Files parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("path") public final String path; // PK
        @JsonProperty("flags") public final long flags;
        @JsonProperty("content") public final byte[] content;
        @JsonProperty("content_checksum") public final String contentChecksum;
        @JsonProperty("alt_suffixes") public final byte[] altSuffixes;

        @JsonIgnore
        public static FilesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new FilesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.Files.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.Files.PATH),
                rawParamsRef.get(GeneratedDatabaseTables.Files.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.Files.CONTENT),
                rawParamsRef.get(GeneratedDatabaseTables.Files.CONTENT_CHECKSUM),
                rawParamsRef.get(GeneratedDatabaseTables.Files.ALT_SUFFIXES)
            );
        }

        @JsonCreator
        public FilesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("path") String pathRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("content") byte[] contentRef,
            @JsonProperty("content_checksum") String contentChecksumRef,
            @JsonProperty("alt_suffixes") byte[] altSuffixesRef
        )
        {
            uuid = uuidRef;
            path = pathRef;
            flags = flagsRef;
            content = contentRef;
            contentChecksum = contentChecksumRef;
            altSuffixes = altSuffixesRef;

            formattedPrimaryKey = String.format(
                FilesSpec.PK_FORMAT,
                path
            );
        }

        @JsonIgnore
        @Override
        public Files getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new Files(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("PATH", path);
            ret.put("FLAGS", flags);
            ret.put("CONTENT", content);
            ret.put("CONTENT_CHECKSUM", contentChecksum);
            ret.put("ALT_SUFFIXES", altSuffixes);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "PATH":
                    return path;
                case "FLAGS":
                    return flags;
                case "CONTENT":
                    return content;
                case "CONTENT_CHECKSUM":
                    return contentChecksum;
                case "ALT_SUFFIXES":
                    return altSuffixes;
                default:
                    throw new ImplementationError("Unknown database column. Table: FILES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.FILES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("files")
    @Singular("files")
    public static class Files extends CustomResource<FilesSpec, Void> implements LinstorCrd<FilesSpec>
    {
        private static final long serialVersionUID = -8008753438235630673L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public Files()
        {
        }

        public Files(FilesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(FilesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static KeyValueStore createKeyValueStore(
        String uuid,
        String kvsName,
        String kvsDspName
    )
    {
        return new KeyValueStore(
            new KeyValueStoreSpec(
                uuid,
                kvsName,
                kvsDspName
            )
        );
    }

    @LinstorData(
        tableName = "KEY_VALUE_STORE"
    )
    @JsonInclude(Include.NON_NULL)
    public static class KeyValueStoreSpec implements LinstorSpec<KeyValueStore, KeyValueStoreSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 6864958112534552289L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable KeyValueStore parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("kvs_name") public final String kvsName; // PK
        @JsonProperty("kvs_dsp_name") public final String kvsDspName;

        @JsonIgnore
        public static KeyValueStoreSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new KeyValueStoreSpec(
                rawParamsRef.get(GeneratedDatabaseTables.KeyValueStore.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.KeyValueStore.KVS_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.KeyValueStore.KVS_DSP_NAME)
            );
        }

        @JsonCreator
        public KeyValueStoreSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("kvs_name") String kvsNameRef,
            @JsonProperty("kvs_dsp_name") String kvsDspNameRef
        )
        {
            uuid = uuidRef;
            kvsName = kvsNameRef;
            kvsDspName = kvsDspNameRef;

            formattedPrimaryKey = String.format(
                KeyValueStoreSpec.PK_FORMAT,
                kvsName
            );
        }

        @JsonIgnore
        @Override
        public KeyValueStore getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new KeyValueStore(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("KVS_NAME", kvsName);
            ret.put("KVS_DSP_NAME", kvsDspName);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "KVS_NAME":
                    return kvsName;
                case "KVS_DSP_NAME":
                    return kvsDspName;
                default:
                    throw new ImplementationError("Unknown database column. Table: KEY_VALUE_STORE, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.KEY_VALUE_STORE;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("keyvaluestore")
    @Singular("keyvaluestore")
    public static class KeyValueStore extends CustomResource<KeyValueStoreSpec, Void> implements LinstorCrd<KeyValueStoreSpec>
    {
        private static final long serialVersionUID = 3951628940590287806L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public KeyValueStore()
        {
        }

        public KeyValueStore(KeyValueStoreSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(KeyValueStoreSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerBcacheVolumes createLayerBcacheVolumes(
        int layerResourceId,
        int vlmNr,
        String nodeName,
        String poolName,
        String devUuid
    )
    {
        return new LayerBcacheVolumes(
            new LayerBcacheVolumesSpec(
                layerResourceId,
                vlmNr,
                nodeName,
                poolName,
                devUuid
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_BCACHE_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerBcacheVolumesSpec implements LinstorSpec<LayerBcacheVolumes, LayerBcacheVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -2233257789667786509L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerBcacheVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("pool_name") public final String poolName;
        @JsonProperty("dev_uuid") public final String devUuid;

        @JsonIgnore
        public static LayerBcacheVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerBcacheVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerBcacheVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerBcacheVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerBcacheVolumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerBcacheVolumes.POOL_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerBcacheVolumes.DEV_UUID)
            );
        }

        @JsonCreator
        public LayerBcacheVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("pool_name") String poolNameRef,
            @JsonProperty("dev_uuid") String devUuidRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            nodeName = nodeNameRef;
            poolName = poolNameRef;
            devUuid = devUuidRef;

            formattedPrimaryKey = String.format(
                LayerBcacheVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerBcacheVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerBcacheVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("NODE_NAME", nodeName);
            ret.put("POOL_NAME", poolName);
            ret.put("DEV_UUID", devUuid);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "NODE_NAME":
                    return nodeName;
                case "POOL_NAME":
                    return poolName;
                case "DEV_UUID":
                    return devUuid;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_BCACHE_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_BCACHE_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerbcachevolumes")
    @Singular("layerbcachevolumes")
    public static class LayerBcacheVolumes extends CustomResource<LayerBcacheVolumesSpec, Void> implements LinstorCrd<LayerBcacheVolumesSpec>
    {
        private static final long serialVersionUID = -8322524123518369855L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerBcacheVolumes()
        {
        }

        public LayerBcacheVolumes(LayerBcacheVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerBcacheVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerCacheVolumes createLayerCacheVolumes(
        int layerResourceId,
        int vlmNr,
        String nodeName,
        String poolNameCache,
        String poolNameMeta
    )
    {
        return new LayerCacheVolumes(
            new LayerCacheVolumesSpec(
                layerResourceId,
                vlmNr,
                nodeName,
                poolNameCache,
                poolNameMeta
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_CACHE_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerCacheVolumesSpec implements LinstorSpec<LayerCacheVolumes, LayerCacheVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5316037476165060513L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerCacheVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("pool_name_cache") public final String poolNameCache;
        @JsonProperty("pool_name_meta") public final String poolNameMeta;

        @JsonIgnore
        public static LayerCacheVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerCacheVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerCacheVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerCacheVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerCacheVolumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerCacheVolumes.POOL_NAME_CACHE),
                rawParamsRef.get(GeneratedDatabaseTables.LayerCacheVolumes.POOL_NAME_META)
            );
        }

        @JsonCreator
        public LayerCacheVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("pool_name_cache") String poolNameCacheRef,
            @JsonProperty("pool_name_meta") String poolNameMetaRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            nodeName = nodeNameRef;
            poolNameCache = poolNameCacheRef;
            poolNameMeta = poolNameMetaRef;

            formattedPrimaryKey = String.format(
                LayerCacheVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerCacheVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerCacheVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("NODE_NAME", nodeName);
            ret.put("POOL_NAME_CACHE", poolNameCache);
            ret.put("POOL_NAME_META", poolNameMeta);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "NODE_NAME":
                    return nodeName;
                case "POOL_NAME_CACHE":
                    return poolNameCache;
                case "POOL_NAME_META":
                    return poolNameMeta;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_CACHE_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_CACHE_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layercachevolumes")
    @Singular("layercachevolumes")
    public static class LayerCacheVolumes extends CustomResource<LayerCacheVolumesSpec, Void> implements LinstorCrd<LayerCacheVolumesSpec>
    {
        private static final long serialVersionUID = 7276441023290229609L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerCacheVolumes()
        {
        }

        public LayerCacheVolumes(LayerCacheVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerCacheVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerDrbdResources createLayerDrbdResources(
        int layerResourceId,
        int peerSlots,
        int alStripes,
        long alStripeSize,
        long flags,
        int nodeId,
        String tcpPortList
    )
    {
        return new LayerDrbdResources(
            new LayerDrbdResourcesSpec(
                layerResourceId,
                peerSlots,
                alStripes,
                alStripeSize,
                flags,
                nodeId,
                tcpPortList
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_DRBD_RESOURCES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerDrbdResourcesSpec implements LinstorSpec<LayerDrbdResources, LayerDrbdResourcesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5715257853558988806L;
        @JsonIgnore private static final String PK_FORMAT = "%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerDrbdResources parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("peer_slots") public final int peerSlots;
        @JsonProperty("al_stripes") public final int alStripes;
        @JsonProperty("al_stripe_size") public final long alStripeSize;
        @JsonProperty("flags") public final long flags;
        @JsonProperty("node_id") public final int nodeId;
        @JsonProperty("tcp_port_list") public final String tcpPortList;

        @JsonIgnore
        public static LayerDrbdResourcesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerDrbdResourcesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.PEER_SLOTS),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.AL_STRIPES),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.AL_STRIPE_SIZE),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.NODE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResources.TCP_PORT_LIST)
            );
        }

        @JsonCreator
        public LayerDrbdResourcesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("peer_slots") int peerSlotsRef,
            @JsonProperty("al_stripes") int alStripesRef,
            @JsonProperty("al_stripe_size") long alStripeSizeRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("node_id") int nodeIdRef,
            @JsonProperty("tcp_port_list") String tcpPortListRef
        )
        {
            layerResourceId = layerResourceIdRef;
            peerSlots = peerSlotsRef;
            alStripes = alStripesRef;
            alStripeSize = alStripeSizeRef;
            flags = flagsRef;
            nodeId = nodeIdRef;
            tcpPortList = tcpPortListRef;

            formattedPrimaryKey = String.format(
                LayerDrbdResourcesSpec.PK_FORMAT,
                layerResourceId
            );
        }

        @JsonIgnore
        @Override
        public LayerDrbdResources getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerDrbdResources(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("PEER_SLOTS", peerSlots);
            ret.put("AL_STRIPES", alStripes);
            ret.put("AL_STRIPE_SIZE", alStripeSize);
            ret.put("FLAGS", flags);
            ret.put("NODE_ID", nodeId);
            ret.put("TCP_PORT_LIST", tcpPortList);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "PEER_SLOTS":
                    return peerSlots;
                case "AL_STRIPES":
                    return alStripes;
                case "AL_STRIPE_SIZE":
                    return alStripeSize;
                case "FLAGS":
                    return flags;
                case "NODE_ID":
                    return nodeId;
                case "TCP_PORT_LIST":
                    return tcpPortList;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_DRBD_RESOURCES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_DRBD_RESOURCES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerdrbdresources")
    @Singular("layerdrbdresources")
    public static class LayerDrbdResources extends CustomResource<LayerDrbdResourcesSpec, Void> implements LinstorCrd<LayerDrbdResourcesSpec>
    {
        private static final long serialVersionUID = 8746299411574829058L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerDrbdResources()
        {
        }

        public LayerDrbdResources(LayerDrbdResourcesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerDrbdResourcesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerDrbdResourceDefinitions createLayerDrbdResourceDefinitions(
        String resourceName,
        String resourceNameSuffix,
        String snapshotName,
        int peerSlots,
        int alStripes,
        long alStripeSize,
        Integer tcpPort,
        String transportType,
        String secret
    )
    {
        return new LayerDrbdResourceDefinitions(
            new LayerDrbdResourceDefinitionsSpec(
                resourceName,
                resourceNameSuffix,
                snapshotName,
                peerSlots,
                alStripes,
                alStripeSize,
                tcpPort,
                transportType,
                secret
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_DRBD_RESOURCE_DEFINITIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerDrbdResourceDefinitionsSpec implements LinstorSpec<LayerDrbdResourceDefinitions, LayerDrbdResourceDefinitionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5192156130180932716L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerDrbdResourceDefinitions parentCrd;

        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("resource_name_suffix") public final String resourceNameSuffix; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("peer_slots") public final int peerSlots;
        @JsonProperty("al_stripes") public final int alStripes;
        @JsonProperty("al_stripe_size") public final long alStripeSize;
        @JsonProperty("tcp_port") public final Integer tcpPort;
        @JsonProperty("transport_type") public final String transportType;
        @JsonProperty("secret") public final String secret;

        @JsonIgnore
        public static LayerDrbdResourceDefinitionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerDrbdResourceDefinitionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.RESOURCE_NAME_SUFFIX),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.PEER_SLOTS),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.AL_STRIPES),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.AL_STRIPE_SIZE),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.TCP_PORT),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.TRANSPORT_TYPE),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdResourceDefinitions.SECRET)
            );
        }

        @JsonCreator
        public LayerDrbdResourceDefinitionsSpec(
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("resource_name_suffix") String resourceNameSuffixRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("peer_slots") int peerSlotsRef,
            @JsonProperty("al_stripes") int alStripesRef,
            @JsonProperty("al_stripe_size") long alStripeSizeRef,
            @JsonProperty("tcp_port") Integer tcpPortRef,
            @JsonProperty("transport_type") String transportTypeRef,
            @JsonProperty("secret") String secretRef
        )
        {
            resourceName = resourceNameRef;
            resourceNameSuffix = resourceNameSuffixRef;
            snapshotName = snapshotNameRef;
            peerSlots = peerSlotsRef;
            alStripes = alStripesRef;
            alStripeSize = alStripeSizeRef;
            tcpPort = tcpPortRef;
            transportType = transportTypeRef;
            secret = secretRef;

            formattedPrimaryKey = String.format(
                LayerDrbdResourceDefinitionsSpec.PK_FORMAT,
                resourceName,
                resourceNameSuffix,
                snapshotName
            );
        }

        @JsonIgnore
        @Override
        public LayerDrbdResourceDefinitions getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerDrbdResourceDefinitions(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("RESOURCE_NAME_SUFFIX", resourceNameSuffix);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("PEER_SLOTS", peerSlots);
            ret.put("AL_STRIPES", alStripes);
            ret.put("AL_STRIPE_SIZE", alStripeSize);
            ret.put("TCP_PORT", tcpPort);
            ret.put("TRANSPORT_TYPE", transportType);
            ret.put("SECRET", secret);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "RESOURCE_NAME":
                    return resourceName;
                case "RESOURCE_NAME_SUFFIX":
                    return resourceNameSuffix;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "PEER_SLOTS":
                    return peerSlots;
                case "AL_STRIPES":
                    return alStripes;
                case "AL_STRIPE_SIZE":
                    return alStripeSize;
                case "TCP_PORT":
                    return tcpPort;
                case "TRANSPORT_TYPE":
                    return transportType;
                case "SECRET":
                    return secret;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_DRBD_RESOURCE_DEFINITIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_DRBD_RESOURCE_DEFINITIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerdrbdresourcedefinitions")
    @Singular("layerdrbdresourcedefinitions")
    public static class LayerDrbdResourceDefinitions extends CustomResource<LayerDrbdResourceDefinitionsSpec, Void> implements LinstorCrd<LayerDrbdResourceDefinitionsSpec>
    {
        private static final long serialVersionUID = 3741062303034338023L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerDrbdResourceDefinitions()
        {
        }

        public LayerDrbdResourceDefinitions(LayerDrbdResourceDefinitionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerDrbdResourceDefinitionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerDrbdVolumes createLayerDrbdVolumes(
        int layerResourceId,
        int vlmNr,
        String nodeName,
        String poolName
    )
    {
        return new LayerDrbdVolumes(
            new LayerDrbdVolumesSpec(
                layerResourceId,
                vlmNr,
                nodeName,
                poolName
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_DRBD_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerDrbdVolumesSpec implements LinstorSpec<LayerDrbdVolumes, LayerDrbdVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -4152016118097970722L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerDrbdVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("pool_name") public final String poolName;

        @JsonIgnore
        public static LayerDrbdVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerDrbdVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumes.POOL_NAME)
            );
        }

        @JsonCreator
        public LayerDrbdVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("pool_name") String poolNameRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            nodeName = nodeNameRef;
            poolName = poolNameRef;

            formattedPrimaryKey = String.format(
                LayerDrbdVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerDrbdVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerDrbdVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("NODE_NAME", nodeName);
            ret.put("POOL_NAME", poolName);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "NODE_NAME":
                    return nodeName;
                case "POOL_NAME":
                    return poolName;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_DRBD_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_DRBD_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerdrbdvolumes")
    @Singular("layerdrbdvolumes")
    public static class LayerDrbdVolumes extends CustomResource<LayerDrbdVolumesSpec, Void> implements LinstorCrd<LayerDrbdVolumesSpec>
    {
        private static final long serialVersionUID = 2906665786299600695L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerDrbdVolumes()
        {
        }

        public LayerDrbdVolumes(LayerDrbdVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerDrbdVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerDrbdVolumeDefinitions createLayerDrbdVolumeDefinitions(
        String resourceName,
        String resourceNameSuffix,
        String snapshotName,
        int vlmNr,
        Integer vlmMinorNr
    )
    {
        return new LayerDrbdVolumeDefinitions(
            new LayerDrbdVolumeDefinitionsSpec(
                resourceName,
                resourceNameSuffix,
                snapshotName,
                vlmNr,
                vlmMinorNr
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_DRBD_VOLUME_DEFINITIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerDrbdVolumeDefinitionsSpec implements LinstorSpec<LayerDrbdVolumeDefinitions, LayerDrbdVolumeDefinitionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -3241046836271074565L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerDrbdVolumeDefinitions parentCrd;

        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("resource_name_suffix") public final String resourceNameSuffix; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("vlm_minor_nr") public final Integer vlmMinorNr;

        @JsonIgnore
        public static LayerDrbdVolumeDefinitionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerDrbdVolumeDefinitionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.RESOURCE_NAME_SUFFIX),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerDrbdVolumeDefinitions.VLM_MINOR_NR)
            );
        }

        @JsonCreator
        public LayerDrbdVolumeDefinitionsSpec(
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("resource_name_suffix") String resourceNameSuffixRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("vlm_minor_nr") Integer vlmMinorNrRef
        )
        {
            resourceName = resourceNameRef;
            resourceNameSuffix = resourceNameSuffixRef;
            snapshotName = snapshotNameRef;
            vlmNr = vlmNrRef;
            vlmMinorNr = vlmMinorNrRef;

            formattedPrimaryKey = String.format(
                LayerDrbdVolumeDefinitionsSpec.PK_FORMAT,
                resourceName,
                resourceNameSuffix,
                snapshotName,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerDrbdVolumeDefinitions getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerDrbdVolumeDefinitions(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("RESOURCE_NAME_SUFFIX", resourceNameSuffix);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("VLM_NR", vlmNr);
            ret.put("VLM_MINOR_NR", vlmMinorNr);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "RESOURCE_NAME":
                    return resourceName;
                case "RESOURCE_NAME_SUFFIX":
                    return resourceNameSuffix;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "VLM_NR":
                    return vlmNr;
                case "VLM_MINOR_NR":
                    return vlmMinorNr;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_DRBD_VOLUME_DEFINITIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_DRBD_VOLUME_DEFINITIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerdrbdvolumedefinitions")
    @Singular("layerdrbdvolumedefinitions")
    public static class LayerDrbdVolumeDefinitions extends CustomResource<LayerDrbdVolumeDefinitionsSpec, Void> implements LinstorCrd<LayerDrbdVolumeDefinitionsSpec>
    {
        private static final long serialVersionUID = -1269749163604623672L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerDrbdVolumeDefinitions()
        {
        }

        public LayerDrbdVolumeDefinitions(LayerDrbdVolumeDefinitionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerDrbdVolumeDefinitionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerLuksVolumes createLayerLuksVolumes(
        int layerResourceId,
        int vlmNr,
        String encryptedPassword
    )
    {
        return new LayerLuksVolumes(
            new LayerLuksVolumesSpec(
                layerResourceId,
                vlmNr,
                encryptedPassword
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_LUKS_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerLuksVolumesSpec implements LinstorSpec<LayerLuksVolumes, LayerLuksVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -5627444534946345972L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerLuksVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("encrypted_password") public final String encryptedPassword;

        @JsonIgnore
        public static LayerLuksVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerLuksVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerLuksVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerLuksVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerLuksVolumes.ENCRYPTED_PASSWORD)
            );
        }

        @JsonCreator
        public LayerLuksVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("encrypted_password") String encryptedPasswordRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            encryptedPassword = encryptedPasswordRef;

            formattedPrimaryKey = String.format(
                LayerLuksVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerLuksVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerLuksVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("ENCRYPTED_PASSWORD", encryptedPassword);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "ENCRYPTED_PASSWORD":
                    return encryptedPassword;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_LUKS_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_LUKS_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerluksvolumes")
    @Singular("layerluksvolumes")
    public static class LayerLuksVolumes extends CustomResource<LayerLuksVolumesSpec, Void> implements LinstorCrd<LayerLuksVolumesSpec>
    {
        private static final long serialVersionUID = 8544909178314755675L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerLuksVolumes()
        {
        }

        public LayerLuksVolumes(LayerLuksVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerLuksVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerResourceIds createLayerResourceIds(
        int layerResourceId,
        String nodeName,
        String resourceName,
        String snapshotName,
        String layerResourceKind,
        Integer layerResourceParentId,
        String layerResourceSuffix,
        boolean layerResourceSuspended
    )
    {
        return new LayerResourceIds(
            new LayerResourceIdsSpec(
                layerResourceId,
                nodeName,
                resourceName,
                snapshotName,
                layerResourceKind,
                layerResourceParentId,
                layerResourceSuffix,
                layerResourceSuspended
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_RESOURCE_IDS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerResourceIdsSpec implements LinstorSpec<LayerResourceIds, LayerResourceIdsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5206053565014086745L;
        @JsonIgnore private static final String PK_FORMAT = "%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerResourceIds parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("resource_name") public final String resourceName;
        @JsonProperty("snapshot_name") public final String snapshotName;
        @JsonProperty("layer_resource_kind") public final String layerResourceKind;
        @JsonProperty("layer_resource_parent_id") public final Integer layerResourceParentId;
        @JsonProperty("layer_resource_suffix") public final String layerResourceSuffix;
        @JsonProperty("layer_resource_suspended") public final boolean layerResourceSuspended;

        @JsonIgnore
        public static LayerResourceIdsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerResourceIdsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_KIND),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_PARENT_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_SUFFIX),
                rawParamsRef.get(GeneratedDatabaseTables.LayerResourceIds.LAYER_RESOURCE_SUSPENDED)
            );
        }

        @JsonCreator
        public LayerResourceIdsSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("layer_resource_kind") String layerResourceKindRef,
            @JsonProperty("layer_resource_parent_id") Integer layerResourceParentIdRef,
            @JsonProperty("layer_resource_suffix") String layerResourceSuffixRef,
            @JsonProperty("layer_resource_suspended") boolean layerResourceSuspendedRef
        )
        {
            layerResourceId = layerResourceIdRef;
            nodeName = nodeNameRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            layerResourceKind = layerResourceKindRef;
            layerResourceParentId = layerResourceParentIdRef;
            layerResourceSuffix = layerResourceSuffixRef;
            layerResourceSuspended = layerResourceSuspendedRef;

            formattedPrimaryKey = String.format(
                LayerResourceIdsSpec.PK_FORMAT,
                layerResourceId
            );
        }

        @JsonIgnore
        @Override
        public LayerResourceIds getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerResourceIds(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("NODE_NAME", nodeName);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("LAYER_RESOURCE_KIND", layerResourceKind);
            ret.put("LAYER_RESOURCE_PARENT_ID", layerResourceParentId);
            ret.put("LAYER_RESOURCE_SUFFIX", layerResourceSuffix);
            ret.put("LAYER_RESOURCE_SUSPENDED", layerResourceSuspended);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "NODE_NAME":
                    return nodeName;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "LAYER_RESOURCE_KIND":
                    return layerResourceKind;
                case "LAYER_RESOURCE_PARENT_ID":
                    return layerResourceParentId;
                case "LAYER_RESOURCE_SUFFIX":
                    return layerResourceSuffix;
                case "LAYER_RESOURCE_SUSPENDED":
                    return layerResourceSuspended;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_RESOURCE_IDS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_RESOURCE_IDS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerresourceids")
    @Singular("layerresourceids")
    public static class LayerResourceIds extends CustomResource<LayerResourceIdsSpec, Void> implements LinstorCrd<LayerResourceIdsSpec>
    {
        private static final long serialVersionUID = 3741348706711011601L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerResourceIds()
        {
        }

        public LayerResourceIds(LayerResourceIdsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerResourceIdsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerStorageVolumes createLayerStorageVolumes(
        int layerResourceId,
        int vlmNr,
        String providerKind,
        String nodeName,
        String storPoolName
    )
    {
        return new LayerStorageVolumes(
            new LayerStorageVolumesSpec(
                layerResourceId,
                vlmNr,
                providerKind,
                nodeName,
                storPoolName
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_STORAGE_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerStorageVolumesSpec implements LinstorSpec<LayerStorageVolumes, LayerStorageVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 7836999439515310264L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerStorageVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("provider_kind") public final String providerKind;
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("stor_pool_name") public final String storPoolName;

        @JsonIgnore
        public static LayerStorageVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerStorageVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerStorageVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerStorageVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerStorageVolumes.PROVIDER_KIND),
                rawParamsRef.get(GeneratedDatabaseTables.LayerStorageVolumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerStorageVolumes.STOR_POOL_NAME)
            );
        }

        @JsonCreator
        public LayerStorageVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("provider_kind") String providerKindRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("stor_pool_name") String storPoolNameRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            providerKind = providerKindRef;
            nodeName = nodeNameRef;
            storPoolName = storPoolNameRef;

            formattedPrimaryKey = String.format(
                LayerStorageVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerStorageVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerStorageVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("PROVIDER_KIND", providerKind);
            ret.put("NODE_NAME", nodeName);
            ret.put("STOR_POOL_NAME", storPoolName);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "PROVIDER_KIND":
                    return providerKind;
                case "NODE_NAME":
                    return nodeName;
                case "STOR_POOL_NAME":
                    return storPoolName;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_STORAGE_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_STORAGE_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerstoragevolumes")
    @Singular("layerstoragevolumes")
    public static class LayerStorageVolumes extends CustomResource<LayerStorageVolumesSpec, Void> implements LinstorCrd<LayerStorageVolumesSpec>
    {
        private static final long serialVersionUID = 4459199404257740574L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerStorageVolumes()
        {
        }

        public LayerStorageVolumes(LayerStorageVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerStorageVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LayerWritecacheVolumes createLayerWritecacheVolumes(
        int layerResourceId,
        int vlmNr,
        String nodeName,
        String poolName
    )
    {
        return new LayerWritecacheVolumes(
            new LayerWritecacheVolumesSpec(
                layerResourceId,
                vlmNr,
                nodeName,
                poolName
            )
        );
    }

    @LinstorData(
        tableName = "LAYER_WRITECACHE_VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LayerWritecacheVolumesSpec implements LinstorSpec<LayerWritecacheVolumes, LayerWritecacheVolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 2681599743403860838L;
        @JsonIgnore private static final String PK_FORMAT = "%d:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LayerWritecacheVolumes parentCrd;

        @JsonProperty("layer_resource_id") public final int layerResourceId; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("node_name") public final String nodeName;
        @JsonProperty("pool_name") public final String poolName;

        @JsonIgnore
        public static LayerWritecacheVolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LayerWritecacheVolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LayerWritecacheVolumes.LAYER_RESOURCE_ID),
                rawParamsRef.get(GeneratedDatabaseTables.LayerWritecacheVolumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.LayerWritecacheVolumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LayerWritecacheVolumes.POOL_NAME)
            );
        }

        @JsonCreator
        public LayerWritecacheVolumesSpec(
            @JsonProperty("layer_resource_id") int layerResourceIdRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("pool_name") String poolNameRef
        )
        {
            layerResourceId = layerResourceIdRef;
            vlmNr = vlmNrRef;
            nodeName = nodeNameRef;
            poolName = poolNameRef;

            formattedPrimaryKey = String.format(
                LayerWritecacheVolumesSpec.PK_FORMAT,
                layerResourceId,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public LayerWritecacheVolumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LayerWritecacheVolumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("LAYER_RESOURCE_ID", layerResourceId);
            ret.put("VLM_NR", vlmNr);
            ret.put("NODE_NAME", nodeName);
            ret.put("POOL_NAME", poolName);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "LAYER_RESOURCE_ID":
                    return layerResourceId;
                case "VLM_NR":
                    return vlmNr;
                case "NODE_NAME":
                    return nodeName;
                case "POOL_NAME":
                    return poolName;
                default:
                    throw new ImplementationError("Unknown database column. Table: LAYER_WRITECACHE_VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LAYER_WRITECACHE_VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("layerwritecachevolumes")
    @Singular("layerwritecachevolumes")
    public static class LayerWritecacheVolumes extends CustomResource<LayerWritecacheVolumesSpec, Void> implements LinstorCrd<LayerWritecacheVolumesSpec>
    {
        private static final long serialVersionUID = -3800189045437549637L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LayerWritecacheVolumes()
        {
        }

        public LayerWritecacheVolumes(LayerWritecacheVolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LayerWritecacheVolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static LinstorRemotes createLinstorRemotes(
        String uuid,
        String name,
        String dspName,
        long flags,
        String url,
        byte[] encryptedPassphrase,
        String clusterId
    )
    {
        return new LinstorRemotes(
            new LinstorRemotesSpec(
                uuid,
                name,
                dspName,
                flags,
                url,
                encryptedPassphrase,
                clusterId
            )
        );
    }

    @LinstorData(
        tableName = "LINSTOR_REMOTES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class LinstorRemotesSpec implements LinstorSpec<LinstorRemotes, LinstorRemotesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 9099166057998066969L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable LinstorRemotes parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("name") public final String name; // PK
        @JsonProperty("dsp_name") public final String dspName;
        @JsonProperty("flags") public final long flags;
        @JsonProperty("url") public final String url;
        @JsonProperty("encrypted_passphrase") public final byte[] encryptedPassphrase;
        @JsonProperty("cluster_id") public final String clusterId;

        @JsonIgnore
        public static LinstorRemotesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new LinstorRemotesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.URL),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.ENCRYPTED_PASSPHRASE),
                rawParamsRef.get(GeneratedDatabaseTables.LinstorRemotes.CLUSTER_ID)
            );
        }

        @JsonCreator
        public LinstorRemotesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("name") String nameRef,
            @JsonProperty("dsp_name") String dspNameRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("url") String urlRef,
            @JsonProperty("encrypted_passphrase") byte[] encryptedPassphraseRef,
            @JsonProperty("cluster_id") String clusterIdRef
        )
        {
            uuid = uuidRef;
            name = nameRef;
            dspName = dspNameRef;
            flags = flagsRef;
            url = urlRef;
            encryptedPassphrase = encryptedPassphraseRef;
            clusterId = clusterIdRef;

            formattedPrimaryKey = String.format(
                LinstorRemotesSpec.PK_FORMAT,
                name
            );
        }

        @JsonIgnore
        @Override
        public LinstorRemotes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new LinstorRemotes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NAME", name);
            ret.put("DSP_NAME", dspName);
            ret.put("FLAGS", flags);
            ret.put("URL", url);
            ret.put("ENCRYPTED_PASSPHRASE", encryptedPassphrase);
            ret.put("CLUSTER_ID", clusterId);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NAME":
                    return name;
                case "DSP_NAME":
                    return dspName;
                case "FLAGS":
                    return flags;
                case "URL":
                    return url;
                case "ENCRYPTED_PASSPHRASE":
                    return encryptedPassphrase;
                case "CLUSTER_ID":
                    return clusterId;
                default:
                    throw new ImplementationError("Unknown database column. Table: LINSTOR_REMOTES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.LINSTOR_REMOTES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("linstorremotes")
    @Singular("linstorremotes")
    public static class LinstorRemotes extends CustomResource<LinstorRemotesSpec, Void> implements LinstorCrd<LinstorRemotesSpec>
    {
        private static final long serialVersionUID = -8808906483105489311L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public LinstorRemotes()
        {
        }

        public LinstorRemotes(LinstorRemotesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(LinstorRemotesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static Nodes createNodes(
        String uuid,
        String nodeName,
        String nodeDspName,
        long nodeFlags,
        int nodeType
    )
    {
        return new Nodes(
            new NodesSpec(
                uuid,
                nodeName,
                nodeDspName,
                nodeFlags,
                nodeType
            )
        );
    }

    @LinstorData(
        tableName = "NODES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class NodesSpec implements LinstorSpec<Nodes, NodesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -3678296081255490731L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable Nodes parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("node_dsp_name") public final String nodeDspName;
        @JsonProperty("node_flags") public final long nodeFlags;
        @JsonProperty("node_type") public final int nodeType;

        @JsonIgnore
        public static NodesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new NodesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.Nodes.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.Nodes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Nodes.NODE_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Nodes.NODE_FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.Nodes.NODE_TYPE)
            );
        }

        @JsonCreator
        public NodesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("node_dsp_name") String nodeDspNameRef,
            @JsonProperty("node_flags") long nodeFlagsRef,
            @JsonProperty("node_type") int nodeTypeRef
        )
        {
            uuid = uuidRef;
            nodeName = nodeNameRef;
            nodeDspName = nodeDspNameRef;
            nodeFlags = nodeFlagsRef;
            nodeType = nodeTypeRef;

            formattedPrimaryKey = String.format(
                NodesSpec.PK_FORMAT,
                nodeName
            );
        }

        @JsonIgnore
        @Override
        public Nodes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new Nodes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME", nodeName);
            ret.put("NODE_DSP_NAME", nodeDspName);
            ret.put("NODE_FLAGS", nodeFlags);
            ret.put("NODE_TYPE", nodeType);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME":
                    return nodeName;
                case "NODE_DSP_NAME":
                    return nodeDspName;
                case "NODE_FLAGS":
                    return nodeFlags;
                case "NODE_TYPE":
                    return nodeType;
                default:
                    throw new ImplementationError("Unknown database column. Table: NODES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.NODES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("nodes")
    @Singular("nodes")
    public static class Nodes extends CustomResource<NodesSpec, Void> implements LinstorCrd<NodesSpec>
    {
        private static final long serialVersionUID = 7951037745999127347L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public Nodes()
        {
        }

        public Nodes(NodesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(NodesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static NodeConnections createNodeConnections(
        String uuid,
        String nodeNameSrc,
        String nodeNameDst
    )
    {
        return new NodeConnections(
            new NodeConnectionsSpec(
                uuid,
                nodeNameSrc,
                nodeNameDst
            )
        );
    }

    @LinstorData(
        tableName = "NODE_CONNECTIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class NodeConnectionsSpec implements LinstorSpec<NodeConnections, NodeConnectionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -5541177337908475906L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable NodeConnections parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name_src") public final String nodeNameSrc; // PK
        @JsonProperty("node_name_dst") public final String nodeNameDst; // PK

        @JsonIgnore
        public static NodeConnectionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new NodeConnectionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.NodeConnections.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.NodeConnections.NODE_NAME_SRC),
                rawParamsRef.get(GeneratedDatabaseTables.NodeConnections.NODE_NAME_DST)
            );
        }

        @JsonCreator
        public NodeConnectionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name_src") String nodeNameSrcRef,
            @JsonProperty("node_name_dst") String nodeNameDstRef
        )
        {
            uuid = uuidRef;
            nodeNameSrc = nodeNameSrcRef;
            nodeNameDst = nodeNameDstRef;

            formattedPrimaryKey = String.format(
                NodeConnectionsSpec.PK_FORMAT,
                nodeNameSrc,
                nodeNameDst
            );
        }

        @JsonIgnore
        @Override
        public NodeConnections getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new NodeConnections(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME_SRC", nodeNameSrc);
            ret.put("NODE_NAME_DST", nodeNameDst);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME_SRC":
                    return nodeNameSrc;
                case "NODE_NAME_DST":
                    return nodeNameDst;
                default:
                    throw new ImplementationError("Unknown database column. Table: NODE_CONNECTIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.NODE_CONNECTIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("nodeconnections")
    @Singular("nodeconnections")
    public static class NodeConnections extends CustomResource<NodeConnectionsSpec, Void> implements LinstorCrd<NodeConnectionsSpec>
    {
        private static final long serialVersionUID = -7911636060010793285L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public NodeConnections()
        {
        }

        public NodeConnections(NodeConnectionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(NodeConnectionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static NodeNetInterfaces createNodeNetInterfaces(
        String uuid,
        String nodeName,
        String nodeNetName,
        String nodeNetDspName,
        String inetAddress,
        Short stltConnPort,
        String stltConnEncrType
    )
    {
        return new NodeNetInterfaces(
            new NodeNetInterfacesSpec(
                uuid,
                nodeName,
                nodeNetName,
                nodeNetDspName,
                inetAddress,
                stltConnPort,
                stltConnEncrType
            )
        );
    }

    @LinstorData(
        tableName = "NODE_NET_INTERFACES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class NodeNetInterfacesSpec implements LinstorSpec<NodeNetInterfaces, NodeNetInterfacesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -4322680105549115858L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable NodeNetInterfaces parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("node_net_name") public final String nodeNetName; // PK
        @JsonProperty("node_net_dsp_name") public final String nodeNetDspName;
        @JsonProperty("inet_address") public final String inetAddress;
        @JsonProperty("stlt_conn_port") public final Short stltConnPort;
        @JsonProperty("stlt_conn_encr_type") public final String stltConnEncrType;

        @JsonIgnore
        public static NodeNetInterfacesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new NodeNetInterfacesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NET_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.NODE_NET_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.INET_ADDRESS),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.STLT_CONN_PORT),
                rawParamsRef.get(GeneratedDatabaseTables.NodeNetInterfaces.STLT_CONN_ENCR_TYPE)
            );
        }

        @JsonCreator
        public NodeNetInterfacesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("node_net_name") String nodeNetNameRef,
            @JsonProperty("node_net_dsp_name") String nodeNetDspNameRef,
            @JsonProperty("inet_address") String inetAddressRef,
            @JsonProperty("stlt_conn_port") Short stltConnPortRef,
            @JsonProperty("stlt_conn_encr_type") String stltConnEncrTypeRef
        )
        {
            uuid = uuidRef;
            nodeName = nodeNameRef;
            nodeNetName = nodeNetNameRef;
            nodeNetDspName = nodeNetDspNameRef;
            inetAddress = inetAddressRef;
            stltConnPort = stltConnPortRef;
            stltConnEncrType = stltConnEncrTypeRef;

            formattedPrimaryKey = String.format(
                NodeNetInterfacesSpec.PK_FORMAT,
                nodeName,
                nodeNetName
            );
        }

        @JsonIgnore
        @Override
        public NodeNetInterfaces getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new NodeNetInterfaces(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME", nodeName);
            ret.put("NODE_NET_NAME", nodeNetName);
            ret.put("NODE_NET_DSP_NAME", nodeNetDspName);
            ret.put("INET_ADDRESS", inetAddress);
            ret.put("STLT_CONN_PORT", stltConnPort);
            ret.put("STLT_CONN_ENCR_TYPE", stltConnEncrType);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME":
                    return nodeName;
                case "NODE_NET_NAME":
                    return nodeNetName;
                case "NODE_NET_DSP_NAME":
                    return nodeNetDspName;
                case "INET_ADDRESS":
                    return inetAddress;
                case "STLT_CONN_PORT":
                    return stltConnPort;
                case "STLT_CONN_ENCR_TYPE":
                    return stltConnEncrType;
                default:
                    throw new ImplementationError("Unknown database column. Table: NODE_NET_INTERFACES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.NODE_NET_INTERFACES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("nodenetinterfaces")
    @Singular("nodenetinterfaces")
    public static class NodeNetInterfaces extends CustomResource<NodeNetInterfacesSpec, Void> implements LinstorCrd<NodeNetInterfacesSpec>
    {
        private static final long serialVersionUID = 2582941207977541848L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public NodeNetInterfaces()
        {
        }

        public NodeNetInterfaces(NodeNetInterfacesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(NodeNetInterfacesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static NodeStorPool createNodeStorPool(
        String uuid,
        String nodeName,
        String poolName,
        String driverName,
        String freeSpaceMgrName,
        String freeSpaceMgrDspName,
        boolean externalLocking
    )
    {
        return new NodeStorPool(
            new NodeStorPoolSpec(
                uuid,
                nodeName,
                poolName,
                driverName,
                freeSpaceMgrName,
                freeSpaceMgrDspName,
                externalLocking
            )
        );
    }

    @LinstorData(
        tableName = "NODE_STOR_POOL"
    )
    @JsonInclude(Include.NON_NULL)
    public static class NodeStorPoolSpec implements LinstorSpec<NodeStorPool, NodeStorPoolSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -7374403482735015864L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable NodeStorPool parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("pool_name") public final String poolName; // PK
        @JsonProperty("driver_name") public final String driverName;
        @JsonProperty("free_space_mgr_name") public final String freeSpaceMgrName;
        @JsonProperty("free_space_mgr_dsp_name") public final String freeSpaceMgrDspName;
        @JsonProperty("external_locking") public final boolean externalLocking;

        @JsonIgnore
        public static NodeStorPoolSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new NodeStorPoolSpec(
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.POOL_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.DRIVER_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.FREE_SPACE_MGR_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.FREE_SPACE_MGR_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.NodeStorPool.EXTERNAL_LOCKING)
            );
        }

        @JsonCreator
        public NodeStorPoolSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("pool_name") String poolNameRef,
            @JsonProperty("driver_name") String driverNameRef,
            @JsonProperty("free_space_mgr_name") String freeSpaceMgrNameRef,
            @JsonProperty("free_space_mgr_dsp_name") String freeSpaceMgrDspNameRef,
            @JsonProperty("external_locking") boolean externalLockingRef
        )
        {
            uuid = uuidRef;
            nodeName = nodeNameRef;
            poolName = poolNameRef;
            driverName = driverNameRef;
            freeSpaceMgrName = freeSpaceMgrNameRef;
            freeSpaceMgrDspName = freeSpaceMgrDspNameRef;
            externalLocking = externalLockingRef;

            formattedPrimaryKey = String.format(
                NodeStorPoolSpec.PK_FORMAT,
                nodeName,
                poolName
            );
        }

        @JsonIgnore
        @Override
        public NodeStorPool getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new NodeStorPool(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME", nodeName);
            ret.put("POOL_NAME", poolName);
            ret.put("DRIVER_NAME", driverName);
            ret.put("FREE_SPACE_MGR_NAME", freeSpaceMgrName);
            ret.put("FREE_SPACE_MGR_DSP_NAME", freeSpaceMgrDspName);
            ret.put("EXTERNAL_LOCKING", externalLocking);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME":
                    return nodeName;
                case "POOL_NAME":
                    return poolName;
                case "DRIVER_NAME":
                    return driverName;
                case "FREE_SPACE_MGR_NAME":
                    return freeSpaceMgrName;
                case "FREE_SPACE_MGR_DSP_NAME":
                    return freeSpaceMgrDspName;
                case "EXTERNAL_LOCKING":
                    return externalLocking;
                default:
                    throw new ImplementationError("Unknown database column. Table: NODE_STOR_POOL, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.NODE_STOR_POOL;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("nodestorpool")
    @Singular("nodestorpool")
    public static class NodeStorPool extends CustomResource<NodeStorPoolSpec, Void> implements LinstorCrd<NodeStorPoolSpec>
    {
        private static final long serialVersionUID = -5989116663194635529L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public NodeStorPool()
        {
        }

        public NodeStorPool(NodeStorPoolSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(NodeStorPoolSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static PropsContainers createPropsContainers(
        String propsInstance,
        String propKey,
        String propValue
    )
    {
        return new PropsContainers(
            new PropsContainersSpec(
                propsInstance,
                propKey,
                propValue
            )
        );
    }

    @LinstorData(
        tableName = "PROPS_CONTAINERS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class PropsContainersSpec implements LinstorSpec<PropsContainers, PropsContainersSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 2942716661868407629L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable PropsContainers parentCrd;

        @JsonProperty("props_instance") public final String propsInstance; // PK
        @JsonProperty("prop_key") public final String propKey; // PK
        @JsonProperty("prop_value") public final String propValue;

        @JsonIgnore
        public static PropsContainersSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new PropsContainersSpec(
                rawParamsRef.get(GeneratedDatabaseTables.PropsContainers.PROPS_INSTANCE),
                rawParamsRef.get(GeneratedDatabaseTables.PropsContainers.PROP_KEY),
                rawParamsRef.get(GeneratedDatabaseTables.PropsContainers.PROP_VALUE)
            );
        }

        @JsonCreator
        public PropsContainersSpec(
            @JsonProperty("props_instance") String propsInstanceRef,
            @JsonProperty("prop_key") String propKeyRef,
            @JsonProperty("prop_value") String propValueRef
        )
        {
            propsInstance = propsInstanceRef;
            propKey = propKeyRef;
            propValue = propValueRef;

            formattedPrimaryKey = String.format(
                PropsContainersSpec.PK_FORMAT,
                propsInstance,
                propKey
            );
        }

        @JsonIgnore
        @Override
        public PropsContainers getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new PropsContainers(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("PROPS_INSTANCE", propsInstance);
            ret.put("PROP_KEY", propKey);
            ret.put("PROP_VALUE", propValue);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "PROPS_INSTANCE":
                    return propsInstance;
                case "PROP_KEY":
                    return propKey;
                case "PROP_VALUE":
                    return propValue;
                default:
                    throw new ImplementationError("Unknown database column. Table: PROPS_CONTAINERS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.PROPS_CONTAINERS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("propscontainers")
    @Singular("propscontainers")
    public static class PropsContainers extends CustomResource<PropsContainersSpec, Void> implements LinstorCrd<PropsContainersSpec>
    {
        private static final long serialVersionUID = -4750081681131254399L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public PropsContainers()
        {
        }

        public PropsContainers(PropsContainersSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(PropsContainersSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static Resources createResources(
        String uuid,
        String nodeName,
        String resourceName,
        String snapshotName,
        long resourceFlags,
        Long createTimestamp
    )
    {
        return new Resources(
            new ResourcesSpec(
                uuid,
                nodeName,
                resourceName,
                snapshotName,
                resourceFlags,
                createTimestamp
            )
        );
    }

    @LinstorData(
        tableName = "RESOURCES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class ResourcesSpec implements LinstorSpec<Resources, ResourcesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 2915259655284411245L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable Resources parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("resource_flags") public final long resourceFlags;
        @JsonProperty("create_timestamp") public final Long createTimestamp;

        @JsonIgnore
        public static ResourcesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new ResourcesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.Resources.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.Resources.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Resources.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Resources.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Resources.RESOURCE_FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.Resources.CREATE_TIMESTAMP)
            );
        }

        @JsonCreator
        public ResourcesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("resource_flags") long resourceFlagsRef,
            @JsonProperty("create_timestamp") Long createTimestampRef
        )
        {
            uuid = uuidRef;
            nodeName = nodeNameRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            resourceFlags = resourceFlagsRef;
            createTimestamp = createTimestampRef;

            formattedPrimaryKey = String.format(
                ResourcesSpec.PK_FORMAT,
                nodeName,
                resourceName,
                snapshotName
            );
        }

        @JsonIgnore
        @Override
        public Resources getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new Resources(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME", nodeName);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("RESOURCE_FLAGS", resourceFlags);
            ret.put("CREATE_TIMESTAMP", createTimestamp);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME":
                    return nodeName;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "RESOURCE_FLAGS":
                    return resourceFlags;
                case "CREATE_TIMESTAMP":
                    return createTimestamp;
                default:
                    throw new ImplementationError("Unknown database column. Table: RESOURCES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.RESOURCES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("resources")
    @Singular("resources")
    public static class Resources extends CustomResource<ResourcesSpec, Void> implements LinstorCrd<ResourcesSpec>
    {
        private static final long serialVersionUID = -8240225378859366252L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public Resources()
        {
        }

        public Resources(ResourcesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(ResourcesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static ResourceConnections createResourceConnections(
        String uuid,
        String nodeNameSrc,
        String nodeNameDst,
        String resourceName,
        String snapshotName,
        long flags,
        Integer tcpPortSrc,
        Integer tcpPortDst
    )
    {
        return new ResourceConnections(
            new ResourceConnectionsSpec(
                uuid,
                nodeNameSrc,
                nodeNameDst,
                resourceName,
                snapshotName,
                flags,
                tcpPortSrc,
                tcpPortDst
            )
        );
    }

    @LinstorData(
        tableName = "RESOURCE_CONNECTIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class ResourceConnectionsSpec implements LinstorSpec<ResourceConnections, ResourceConnectionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 1225325956156397025L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable ResourceConnections parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name_src") public final String nodeNameSrc; // PK
        @JsonProperty("node_name_dst") public final String nodeNameDst; // PK
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("flags") public final long flags;
        @JsonProperty("tcp_port_src") public final Integer tcpPortSrc;
        @JsonProperty("tcp_port_dst") public final Integer tcpPortDst;

        @JsonIgnore
        public static ResourceConnectionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new ResourceConnectionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.NODE_NAME_SRC),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.NODE_NAME_DST),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.TCP_PORT_SRC),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceConnections.TCP_PORT_DST)
            );
        }

        @JsonCreator
        public ResourceConnectionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name_src") String nodeNameSrcRef,
            @JsonProperty("node_name_dst") String nodeNameDstRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("tcp_port_src") Integer tcpPortSrcRef,
            @JsonProperty("tcp_port_dst") Integer tcpPortDstRef
        )
        {
            uuid = uuidRef;
            nodeNameSrc = nodeNameSrcRef;
            nodeNameDst = nodeNameDstRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            flags = flagsRef;
            tcpPortSrc = tcpPortSrcRef;
            tcpPortDst = tcpPortDstRef;

            formattedPrimaryKey = String.format(
                ResourceConnectionsSpec.PK_FORMAT,
                nodeNameSrc,
                nodeNameDst,
                resourceName,
                snapshotName
            );
        }

        @JsonIgnore
        @Override
        public ResourceConnections getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new ResourceConnections(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME_SRC", nodeNameSrc);
            ret.put("NODE_NAME_DST", nodeNameDst);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("FLAGS", flags);
            ret.put("TCP_PORT_SRC", tcpPortSrc);
            ret.put("TCP_PORT_DST", tcpPortDst);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME_SRC":
                    return nodeNameSrc;
                case "NODE_NAME_DST":
                    return nodeNameDst;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "FLAGS":
                    return flags;
                case "TCP_PORT_SRC":
                    return tcpPortSrc;
                case "TCP_PORT_DST":
                    return tcpPortDst;
                default:
                    throw new ImplementationError("Unknown database column. Table: RESOURCE_CONNECTIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.RESOURCE_CONNECTIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("resourceconnections")
    @Singular("resourceconnections")
    public static class ResourceConnections extends CustomResource<ResourceConnectionsSpec, Void> implements LinstorCrd<ResourceConnectionsSpec>
    {
        private static final long serialVersionUID = 7166321391504741364L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public ResourceConnections()
        {
        }

        public ResourceConnections(ResourceConnectionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(ResourceConnectionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static ResourceDefinitions createResourceDefinitions(
        String uuid,
        String resourceName,
        String snapshotName,
        String resourceDspName,
        String snapshotDspName,
        long resourceFlags,
        String layerStack,
        byte[] resourceExternalName,
        String resourceGroupName,
        String parentUuid
    )
    {
        return new ResourceDefinitions(
            new ResourceDefinitionsSpec(
                uuid,
                resourceName,
                snapshotName,
                resourceDspName,
                snapshotDspName,
                resourceFlags,
                layerStack,
                resourceExternalName,
                resourceGroupName,
                parentUuid
            )
        );
    }

    @LinstorData(
        tableName = "RESOURCE_DEFINITIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class ResourceDefinitionsSpec implements LinstorSpec<ResourceDefinitions, ResourceDefinitionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 1926219090543301623L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable ResourceDefinitions parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("resource_dsp_name") public final String resourceDspName;
        @JsonProperty("snapshot_dsp_name") public final String snapshotDspName;
        @JsonProperty("resource_flags") public final long resourceFlags;
        @JsonProperty("layer_stack") public final String layerStack;
        @JsonProperty("resource_external_name") public final byte[] resourceExternalName;
        @JsonProperty("resource_group_name") public final String resourceGroupName;
        @JsonProperty("parent_uuid") public final String parentUuid;

        @JsonIgnore
        public static ResourceDefinitionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new ResourceDefinitionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.SNAPSHOT_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.LAYER_STACK),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_EXTERNAL_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.RESOURCE_GROUP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceDefinitions.PARENT_UUID)
            );
        }

        @JsonCreator
        public ResourceDefinitionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("resource_dsp_name") String resourceDspNameRef,
            @JsonProperty("snapshot_dsp_name") String snapshotDspNameRef,
            @JsonProperty("resource_flags") long resourceFlagsRef,
            @JsonProperty("layer_stack") String layerStackRef,
            @JsonProperty("resource_external_name") byte[] resourceExternalNameRef,
            @JsonProperty("resource_group_name") String resourceGroupNameRef,
            @JsonProperty("parent_uuid") String parentUuidRef
        )
        {
            uuid = uuidRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            resourceDspName = resourceDspNameRef;
            snapshotDspName = snapshotDspNameRef;
            resourceFlags = resourceFlagsRef;
            layerStack = layerStackRef;
            resourceExternalName = resourceExternalNameRef;
            resourceGroupName = resourceGroupNameRef;
            parentUuid = parentUuidRef;

            formattedPrimaryKey = String.format(
                ResourceDefinitionsSpec.PK_FORMAT,
                resourceName,
                snapshotName
            );
        }

        @JsonIgnore
        @Override
        public ResourceDefinitions getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new ResourceDefinitions(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("RESOURCE_DSP_NAME", resourceDspName);
            ret.put("SNAPSHOT_DSP_NAME", snapshotDspName);
            ret.put("RESOURCE_FLAGS", resourceFlags);
            ret.put("LAYER_STACK", layerStack);
            ret.put("RESOURCE_EXTERNAL_NAME", resourceExternalName);
            ret.put("RESOURCE_GROUP_NAME", resourceGroupName);
            ret.put("PARENT_UUID", parentUuid);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "RESOURCE_DSP_NAME":
                    return resourceDspName;
                case "SNAPSHOT_DSP_NAME":
                    return snapshotDspName;
                case "RESOURCE_FLAGS":
                    return resourceFlags;
                case "LAYER_STACK":
                    return layerStack;
                case "RESOURCE_EXTERNAL_NAME":
                    return resourceExternalName;
                case "RESOURCE_GROUP_NAME":
                    return resourceGroupName;
                case "PARENT_UUID":
                    return parentUuid;
                default:
                    throw new ImplementationError("Unknown database column. Table: RESOURCE_DEFINITIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.RESOURCE_DEFINITIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("resourcedefinitions")
    @Singular("resourcedefinitions")
    public static class ResourceDefinitions extends CustomResource<ResourceDefinitionsSpec, Void> implements LinstorCrd<ResourceDefinitionsSpec>
    {
        private static final long serialVersionUID = 1721378826426753603L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public ResourceDefinitions()
        {
        }

        public ResourceDefinitions(ResourceDefinitionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(ResourceDefinitionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static ResourceGroups createResourceGroups(
        String uuid,
        String resourceGroupName,
        String resourceGroupDspName,
        String description,
        String layerStack,
        int replicaCount,
        String nodeNameList,
        String poolName,
        String poolNameDiskless,
        String doNotPlaceWithRscRegex,
        String doNotPlaceWithRscList,
        String replicasOnSame,
        String replicasOnDifferent,
        String xReplicasOnDifferent,
        String allowedProviderList,
        Boolean disklessOnRemaining,
        Short peerSlots
    )
    {
        return new ResourceGroups(
            new ResourceGroupsSpec(
                uuid,
                resourceGroupName,
                resourceGroupDspName,
                description,
                layerStack,
                replicaCount,
                nodeNameList,
                poolName,
                poolNameDiskless,
                doNotPlaceWithRscRegex,
                doNotPlaceWithRscList,
                replicasOnSame,
                replicasOnDifferent,
                xReplicasOnDifferent,
                allowedProviderList,
                disklessOnRemaining,
                peerSlots
            )
        );
    }

    @LinstorData(
        tableName = "RESOURCE_GROUPS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class ResourceGroupsSpec implements LinstorSpec<ResourceGroups, ResourceGroupsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 6763203681976552302L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable ResourceGroups parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("resource_group_name") public final String resourceGroupName; // PK
        @JsonProperty("resource_group_dsp_name") public final String resourceGroupDspName;
        @JsonProperty("description") public final String description;
        @JsonProperty("layer_stack") public final String layerStack;
        @JsonProperty("replica_count") public final int replicaCount;
        @JsonProperty("node_name_list") public final String nodeNameList;
        @JsonProperty("pool_name") public final String poolName;
        @JsonProperty("pool_name_diskless") public final String poolNameDiskless;
        @JsonProperty("do_not_place_with_rsc_regex") public final String doNotPlaceWithRscRegex;
        @JsonProperty("do_not_place_with_rsc_list") public final String doNotPlaceWithRscList;
        @JsonProperty("replicas_on_same") public final String replicasOnSame;
        @JsonProperty("replicas_on_different") public final String replicasOnDifferent;
        @JsonProperty("x_replicas_on_different") public final String xReplicasOnDifferent;
        @JsonProperty("allowed_provider_list") public final String allowedProviderList;
        @JsonProperty("diskless_on_remaining") public final Boolean disklessOnRemaining;
        @JsonProperty("peer_slots") public final Short peerSlots;

        @JsonIgnore
        public static ResourceGroupsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new ResourceGroupsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.RESOURCE_GROUP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.RESOURCE_GROUP_DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.DESCRIPTION),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.LAYER_STACK),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.REPLICA_COUNT),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.NODE_NAME_LIST),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.POOL_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.POOL_NAME_DISKLESS),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.DO_NOT_PLACE_WITH_RSC_REGEX),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.DO_NOT_PLACE_WITH_RSC_LIST),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.REPLICAS_ON_SAME),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.REPLICAS_ON_DIFFERENT),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.X_REPLICAS_ON_DIFFERENT),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.ALLOWED_PROVIDER_LIST),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.DISKLESS_ON_REMAINING),
                rawParamsRef.get(GeneratedDatabaseTables.ResourceGroups.PEER_SLOTS)
            );
        }

        @JsonCreator
        public ResourceGroupsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("resource_group_name") String resourceGroupNameRef,
            @JsonProperty("resource_group_dsp_name") String resourceGroupDspNameRef,
            @JsonProperty("description") String descriptionRef,
            @JsonProperty("layer_stack") String layerStackRef,
            @JsonProperty("replica_count") int replicaCountRef,
            @JsonProperty("node_name_list") String nodeNameListRef,
            @JsonProperty("pool_name") String poolNameRef,
            @JsonProperty("pool_name_diskless") String poolNameDisklessRef,
            @JsonProperty("do_not_place_with_rsc_regex") String doNotPlaceWithRscRegexRef,
            @JsonProperty("do_not_place_with_rsc_list") String doNotPlaceWithRscListRef,
            @JsonProperty("replicas_on_same") String replicasOnSameRef,
            @JsonProperty("replicas_on_different") String replicasOnDifferentRef,
            @JsonProperty("x_replicas_on_different") String xReplicasOnDifferentRef,
            @JsonProperty("allowed_provider_list") String allowedProviderListRef,
            @JsonProperty("diskless_on_remaining") Boolean disklessOnRemainingRef,
            @JsonProperty("peer_slots") Short peerSlotsRef
        )
        {
            uuid = uuidRef;
            resourceGroupName = resourceGroupNameRef;
            resourceGroupDspName = resourceGroupDspNameRef;
            description = descriptionRef;
            layerStack = layerStackRef;
            replicaCount = replicaCountRef;
            nodeNameList = nodeNameListRef;
            poolName = poolNameRef;
            poolNameDiskless = poolNameDisklessRef;
            doNotPlaceWithRscRegex = doNotPlaceWithRscRegexRef;
            doNotPlaceWithRscList = doNotPlaceWithRscListRef;
            replicasOnSame = replicasOnSameRef;
            replicasOnDifferent = replicasOnDifferentRef;
            xReplicasOnDifferent = xReplicasOnDifferentRef;
            allowedProviderList = allowedProviderListRef;
            disklessOnRemaining = disklessOnRemainingRef;
            peerSlots = peerSlotsRef;

            formattedPrimaryKey = String.format(
                ResourceGroupsSpec.PK_FORMAT,
                resourceGroupName
            );
        }

        @JsonIgnore
        @Override
        public ResourceGroups getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new ResourceGroups(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("RESOURCE_GROUP_NAME", resourceGroupName);
            ret.put("RESOURCE_GROUP_DSP_NAME", resourceGroupDspName);
            ret.put("DESCRIPTION", description);
            ret.put("LAYER_STACK", layerStack);
            ret.put("REPLICA_COUNT", replicaCount);
            ret.put("NODE_NAME_LIST", nodeNameList);
            ret.put("POOL_NAME", poolName);
            ret.put("POOL_NAME_DISKLESS", poolNameDiskless);
            ret.put("DO_NOT_PLACE_WITH_RSC_REGEX", doNotPlaceWithRscRegex);
            ret.put("DO_NOT_PLACE_WITH_RSC_LIST", doNotPlaceWithRscList);
            ret.put("REPLICAS_ON_SAME", replicasOnSame);
            ret.put("REPLICAS_ON_DIFFERENT", replicasOnDifferent);
            ret.put("X_REPLICAS_ON_DIFFERENT", xReplicasOnDifferent);
            ret.put("ALLOWED_PROVIDER_LIST", allowedProviderList);
            ret.put("DISKLESS_ON_REMAINING", disklessOnRemaining);
            ret.put("PEER_SLOTS", peerSlots);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "RESOURCE_GROUP_NAME":
                    return resourceGroupName;
                case "RESOURCE_GROUP_DSP_NAME":
                    return resourceGroupDspName;
                case "DESCRIPTION":
                    return description;
                case "LAYER_STACK":
                    return layerStack;
                case "REPLICA_COUNT":
                    return replicaCount;
                case "NODE_NAME_LIST":
                    return nodeNameList;
                case "POOL_NAME":
                    return poolName;
                case "POOL_NAME_DISKLESS":
                    return poolNameDiskless;
                case "DO_NOT_PLACE_WITH_RSC_REGEX":
                    return doNotPlaceWithRscRegex;
                case "DO_NOT_PLACE_WITH_RSC_LIST":
                    return doNotPlaceWithRscList;
                case "REPLICAS_ON_SAME":
                    return replicasOnSame;
                case "REPLICAS_ON_DIFFERENT":
                    return replicasOnDifferent;
                case "X_REPLICAS_ON_DIFFERENT":
                    return xReplicasOnDifferent;
                case "ALLOWED_PROVIDER_LIST":
                    return allowedProviderList;
                case "DISKLESS_ON_REMAINING":
                    return disklessOnRemaining;
                case "PEER_SLOTS":
                    return peerSlots;
                default:
                    throw new ImplementationError("Unknown database column. Table: RESOURCE_GROUPS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.RESOURCE_GROUPS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("resourcegroups")
    @Singular("resourcegroups")
    public static class ResourceGroups extends CustomResource<ResourceGroupsSpec, Void> implements LinstorCrd<ResourceGroupsSpec>
    {
        private static final long serialVersionUID = -7424365943293709456L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public ResourceGroups()
        {
        }

        public ResourceGroups(ResourceGroupsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(ResourceGroupsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static S3Remotes createS3Remotes(
        String uuid,
        String name,
        String dspName,
        long flags,
        String endpoint,
        String bucket,
        String region,
        byte[] accessKey,
        byte[] secretKey
    )
    {
        return new S3Remotes(
            new S3RemotesSpec(
                uuid,
                name,
                dspName,
                flags,
                endpoint,
                bucket,
                region,
                accessKey,
                secretKey
            )
        );
    }

    @LinstorData(
        tableName = "S3_REMOTES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class S3RemotesSpec implements LinstorSpec<S3Remotes, S3RemotesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -2434114007867961341L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable S3Remotes parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("name") public final String name; // PK
        @JsonProperty("dsp_name") public final String dspName;
        @JsonProperty("flags") public final long flags;
        @JsonProperty("endpoint") public final String endpoint;
        @JsonProperty("bucket") public final String bucket;
        @JsonProperty("region") public final String region;
        @JsonProperty("access_key") public final byte[] accessKey;
        @JsonProperty("secret_key") public final byte[] secretKey;

        @JsonIgnore
        public static S3RemotesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new S3RemotesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.NAME),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.ENDPOINT),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.BUCKET),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.REGION),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.ACCESS_KEY),
                rawParamsRef.get(GeneratedDatabaseTables.S3Remotes.SECRET_KEY)
            );
        }

        @JsonCreator
        public S3RemotesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("name") String nameRef,
            @JsonProperty("dsp_name") String dspNameRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("endpoint") String endpointRef,
            @JsonProperty("bucket") String bucketRef,
            @JsonProperty("region") String regionRef,
            @JsonProperty("access_key") byte[] accessKeyRef,
            @JsonProperty("secret_key") byte[] secretKeyRef
        )
        {
            uuid = uuidRef;
            name = nameRef;
            dspName = dspNameRef;
            flags = flagsRef;
            endpoint = endpointRef;
            bucket = bucketRef;
            region = regionRef;
            accessKey = accessKeyRef;
            secretKey = secretKeyRef;

            formattedPrimaryKey = String.format(
                S3RemotesSpec.PK_FORMAT,
                name
            );
        }

        @JsonIgnore
        @Override
        public S3Remotes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new S3Remotes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NAME", name);
            ret.put("DSP_NAME", dspName);
            ret.put("FLAGS", flags);
            ret.put("ENDPOINT", endpoint);
            ret.put("BUCKET", bucket);
            ret.put("REGION", region);
            ret.put("ACCESS_KEY", accessKey);
            ret.put("SECRET_KEY", secretKey);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NAME":
                    return name;
                case "DSP_NAME":
                    return dspName;
                case "FLAGS":
                    return flags;
                case "ENDPOINT":
                    return endpoint;
                case "BUCKET":
                    return bucket;
                case "REGION":
                    return region;
                case "ACCESS_KEY":
                    return accessKey;
                case "SECRET_KEY":
                    return secretKey;
                default:
                    throw new ImplementationError("Unknown database column. Table: S3_REMOTES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.S3_REMOTES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("s3remotes")
    @Singular("s3remotes")
    public static class S3Remotes extends CustomResource<S3RemotesSpec, Void> implements LinstorCrd<S3RemotesSpec>
    {
        private static final long serialVersionUID = -5024661244020698515L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public S3Remotes()
        {
        }

        public S3Remotes(S3RemotesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(S3RemotesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static SatellitesCapacity createSatellitesCapacity(
        String nodeName,
        byte[] capacity,
        boolean failFlag,
        byte[] allocated,
        byte[] usable
    )
    {
        return new SatellitesCapacity(
            new SatellitesCapacitySpec(
                nodeName,
                capacity,
                failFlag,
                allocated,
                usable
            )
        );
    }

    @LinstorData(
        tableName = "SATELLITES_CAPACITY"
    )
    @JsonInclude(Include.NON_NULL)
    public static class SatellitesCapacitySpec implements LinstorSpec<SatellitesCapacity, SatellitesCapacitySpec>
    {
        @JsonIgnore private static final long serialVersionUID = -8248863884336883125L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable SatellitesCapacity parentCrd;

        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("capacity") public final byte[] capacity;
        @JsonProperty("fail_flag") public final boolean failFlag;
        @JsonProperty("allocated") public final byte[] allocated;
        @JsonProperty("usable") public final byte[] usable;

        @JsonIgnore
        public static SatellitesCapacitySpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new SatellitesCapacitySpec(
                rawParamsRef.get(GeneratedDatabaseTables.SatellitesCapacity.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.SatellitesCapacity.CAPACITY),
                rawParamsRef.get(GeneratedDatabaseTables.SatellitesCapacity.FAIL_FLAG),
                rawParamsRef.get(GeneratedDatabaseTables.SatellitesCapacity.ALLOCATED),
                rawParamsRef.get(GeneratedDatabaseTables.SatellitesCapacity.USABLE)
            );
        }

        @JsonCreator
        public SatellitesCapacitySpec(
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("capacity") byte[] capacityRef,
            @JsonProperty("fail_flag") boolean failFlagRef,
            @JsonProperty("allocated") byte[] allocatedRef,
            @JsonProperty("usable") byte[] usableRef
        )
        {
            nodeName = nodeNameRef;
            capacity = capacityRef;
            failFlag = failFlagRef;
            allocated = allocatedRef;
            usable = usableRef;

            formattedPrimaryKey = String.format(
                SatellitesCapacitySpec.PK_FORMAT,
                nodeName
            );
        }

        @JsonIgnore
        @Override
        public SatellitesCapacity getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new SatellitesCapacity(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("NODE_NAME", nodeName);
            ret.put("CAPACITY", capacity);
            ret.put("FAIL_FLAG", failFlag);
            ret.put("ALLOCATED", allocated);
            ret.put("USABLE", usable);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "NODE_NAME":
                    return nodeName;
                case "CAPACITY":
                    return capacity;
                case "FAIL_FLAG":
                    return failFlag;
                case "ALLOCATED":
                    return allocated;
                case "USABLE":
                    return usable;
                default:
                    throw new ImplementationError("Unknown database column. Table: SATELLITES_CAPACITY, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.SATELLITES_CAPACITY;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("satellitescapacity")
    @Singular("satellitescapacity")
    public static class SatellitesCapacity extends CustomResource<SatellitesCapacitySpec, Void> implements LinstorCrd<SatellitesCapacitySpec>
    {
        private static final long serialVersionUID = 2552710262850353700L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public SatellitesCapacity()
        {
        }

        public SatellitesCapacity(SatellitesCapacitySpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(SatellitesCapacitySpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static Schedules createSchedules(
        String uuid,
        String name,
        String dspName,
        long flags,
        String fullCron,
        String incCron,
        Integer keepLocal,
        Integer keepRemote,
        long onFailure,
        Integer maxRetries
    )
    {
        return new Schedules(
            new SchedulesSpec(
                uuid,
                name,
                dspName,
                flags,
                fullCron,
                incCron,
                keepLocal,
                keepRemote,
                onFailure,
                maxRetries
            )
        );
    }

    @LinstorData(
        tableName = "SCHEDULES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class SchedulesSpec implements LinstorSpec<Schedules, SchedulesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -4392271747086370877L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable Schedules parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("name") public final String name; // PK
        @JsonProperty("dsp_name") public final String dspName;
        @JsonProperty("flags") public final long flags;
        @JsonProperty("full_cron") public final String fullCron;
        @JsonProperty("inc_cron") public final String incCron;
        @JsonProperty("keep_local") public final Integer keepLocal;
        @JsonProperty("keep_remote") public final Integer keepRemote;
        @JsonProperty("on_failure") public final long onFailure;
        @JsonProperty("max_retries") public final Integer maxRetries;

        @JsonIgnore
        public static SchedulesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new SchedulesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.DSP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.FLAGS),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.FULL_CRON),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.INC_CRON),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.KEEP_LOCAL),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.KEEP_REMOTE),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.ON_FAILURE),
                rawParamsRef.get(GeneratedDatabaseTables.Schedules.MAX_RETRIES)
            );
        }

        @JsonCreator
        public SchedulesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("name") String nameRef,
            @JsonProperty("dsp_name") String dspNameRef,
            @JsonProperty("flags") long flagsRef,
            @JsonProperty("full_cron") String fullCronRef,
            @JsonProperty("inc_cron") String incCronRef,
            @JsonProperty("keep_local") Integer keepLocalRef,
            @JsonProperty("keep_remote") Integer keepRemoteRef,
            @JsonProperty("on_failure") long onFailureRef,
            @JsonProperty("max_retries") Integer maxRetriesRef
        )
        {
            uuid = uuidRef;
            name = nameRef;
            dspName = dspNameRef;
            flags = flagsRef;
            fullCron = fullCronRef;
            incCron = incCronRef;
            keepLocal = keepLocalRef;
            keepRemote = keepRemoteRef;
            onFailure = onFailureRef;
            maxRetries = maxRetriesRef;

            formattedPrimaryKey = String.format(
                SchedulesSpec.PK_FORMAT,
                name
            );
        }

        @JsonIgnore
        @Override
        public Schedules getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new Schedules(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NAME", name);
            ret.put("DSP_NAME", dspName);
            ret.put("FLAGS", flags);
            ret.put("FULL_CRON", fullCron);
            ret.put("INC_CRON", incCron);
            ret.put("KEEP_LOCAL", keepLocal);
            ret.put("KEEP_REMOTE", keepRemote);
            ret.put("ON_FAILURE", onFailure);
            ret.put("MAX_RETRIES", maxRetries);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NAME":
                    return name;
                case "DSP_NAME":
                    return dspName;
                case "FLAGS":
                    return flags;
                case "FULL_CRON":
                    return fullCron;
                case "INC_CRON":
                    return incCron;
                case "KEEP_LOCAL":
                    return keepLocal;
                case "KEEP_REMOTE":
                    return keepRemote;
                case "ON_FAILURE":
                    return onFailure;
                case "MAX_RETRIES":
                    return maxRetries;
                default:
                    throw new ImplementationError("Unknown database column. Table: SCHEDULES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.SCHEDULES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("schedules")
    @Singular("schedules")
    public static class Schedules extends CustomResource<SchedulesSpec, Void> implements LinstorCrd<SchedulesSpec>
    {
        private static final long serialVersionUID = 2199544927873045931L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public Schedules()
        {
        }

        public Schedules(SchedulesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(SchedulesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static SpaceHistory createSpaceHistory(
        long entryDate,
        byte[] capacity
    )
    {
        return new SpaceHistory(
            new SpaceHistorySpec(
                entryDate,
                capacity
            )
        );
    }

    @LinstorData(
        tableName = "SPACE_HISTORY"
    )
    @JsonInclude(Include.NON_NULL)
    public static class SpaceHistorySpec implements LinstorSpec<SpaceHistory, SpaceHistorySpec>
    {
        @JsonIgnore private static final long serialVersionUID = 530924817771898917L;
        @JsonIgnore private static final String PK_FORMAT = "%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable SpaceHistory parentCrd;

        @JsonProperty("entry_date") public final long entryDate; // PK
        @JsonProperty("capacity") public final byte[] capacity;

        @JsonIgnore
        public static SpaceHistorySpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new SpaceHistorySpec(
                rawParamsRef.get(GeneratedDatabaseTables.SpaceHistory.ENTRY_DATE),
                rawParamsRef.get(GeneratedDatabaseTables.SpaceHistory.CAPACITY)
            );
        }

        @JsonCreator
        public SpaceHistorySpec(
            @JsonProperty("entry_date") long entryDateRef,
            @JsonProperty("capacity") byte[] capacityRef
        )
        {
            entryDate = entryDateRef;
            capacity = capacityRef;

            formattedPrimaryKey = String.format(
                SpaceHistorySpec.PK_FORMAT,
                entryDate
            );
        }

        @JsonIgnore
        @Override
        public SpaceHistory getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new SpaceHistory(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("ENTRY_DATE", entryDate);
            ret.put("CAPACITY", capacity);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "ENTRY_DATE":
                    return entryDate;
                case "CAPACITY":
                    return capacity;
                default:
                    throw new ImplementationError("Unknown database column. Table: SPACE_HISTORY, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.SPACE_HISTORY;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("spacehistory")
    @Singular("spacehistory")
    public static class SpaceHistory extends CustomResource<SpaceHistorySpec, Void> implements LinstorCrd<SpaceHistorySpec>
    {
        private static final long serialVersionUID = 4058294131270406069L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public SpaceHistory()
        {
        }

        public SpaceHistory(SpaceHistorySpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(SpaceHistorySpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static StorPoolDefinitions createStorPoolDefinitions(
        String uuid,
        String poolName,
        String poolDspName
    )
    {
        return new StorPoolDefinitions(
            new StorPoolDefinitionsSpec(
                uuid,
                poolName,
                poolDspName
            )
        );
    }

    @LinstorData(
        tableName = "STOR_POOL_DEFINITIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class StorPoolDefinitionsSpec implements LinstorSpec<StorPoolDefinitions, StorPoolDefinitionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 9005656234011982844L;
        @JsonIgnore private static final String PK_FORMAT = "%s";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable StorPoolDefinitions parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("pool_name") public final String poolName; // PK
        @JsonProperty("pool_dsp_name") public final String poolDspName;

        @JsonIgnore
        public static StorPoolDefinitionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new StorPoolDefinitionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.StorPoolDefinitions.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.StorPoolDefinitions.POOL_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.StorPoolDefinitions.POOL_DSP_NAME)
            );
        }

        @JsonCreator
        public StorPoolDefinitionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("pool_name") String poolNameRef,
            @JsonProperty("pool_dsp_name") String poolDspNameRef
        )
        {
            uuid = uuidRef;
            poolName = poolNameRef;
            poolDspName = poolDspNameRef;

            formattedPrimaryKey = String.format(
                StorPoolDefinitionsSpec.PK_FORMAT,
                poolName
            );
        }

        @JsonIgnore
        @Override
        public StorPoolDefinitions getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new StorPoolDefinitions(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("POOL_NAME", poolName);
            ret.put("POOL_DSP_NAME", poolDspName);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "POOL_NAME":
                    return poolName;
                case "POOL_DSP_NAME":
                    return poolDspName;
                default:
                    throw new ImplementationError("Unknown database column. Table: STOR_POOL_DEFINITIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.STOR_POOL_DEFINITIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("storpooldefinitions")
    @Singular("storpooldefinitions")
    public static class StorPoolDefinitions extends CustomResource<StorPoolDefinitionsSpec, Void> implements LinstorCrd<StorPoolDefinitionsSpec>
    {
        private static final long serialVersionUID = -31034235178982068L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public StorPoolDefinitions()
        {
        }

        public StorPoolDefinitions(StorPoolDefinitionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(StorPoolDefinitionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static TrackingDate createTrackingDate(
        long entryDate
    )
    {
        return new TrackingDate(
            new TrackingDateSpec(
                entryDate
            )
        );
    }

    @LinstorData(
        tableName = "TRACKING_DATE"
    )
    @JsonInclude(Include.NON_NULL)
    public static class TrackingDateSpec implements LinstorSpec<TrackingDate, TrackingDateSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 7300262506562979962L;
        @JsonIgnore private static final String PK_FORMAT = "%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable TrackingDate parentCrd;

        // No PK found. Combining ALL columns for K8s key
        @JsonProperty("entry_date") public final long entryDate;

        @JsonIgnore
        public static TrackingDateSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new TrackingDateSpec(
                rawParamsRef.get(GeneratedDatabaseTables.TrackingDate.ENTRY_DATE)
            );
        }

        @JsonCreator
        public TrackingDateSpec(
            @JsonProperty("entry_date") long entryDateRef
        )
        {
            entryDate = entryDateRef;

            formattedPrimaryKey = String.format(
                TrackingDateSpec.PK_FORMAT,
                entryDate
            );
        }

        @JsonIgnore
        @Override
        public TrackingDate getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new TrackingDate(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("ENTRY_DATE", entryDate);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "ENTRY_DATE":
                    return entryDate;
                default:
                    throw new ImplementationError("Unknown database column. Table: TRACKING_DATE, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.TRACKING_DATE;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("trackingdate")
    @Singular("trackingdate")
    public static class TrackingDate extends CustomResource<TrackingDateSpec, Void> implements LinstorCrd<TrackingDateSpec>
    {
        private static final long serialVersionUID = -2049618036383731780L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public TrackingDate()
        {
        }

        public TrackingDate(TrackingDateSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(TrackingDateSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static Volumes createVolumes(
        String uuid,
        String nodeName,
        String resourceName,
        String snapshotName,
        int vlmNr,
        long vlmFlags
    )
    {
        return new Volumes(
            new VolumesSpec(
                uuid,
                nodeName,
                resourceName,
                snapshotName,
                vlmNr,
                vlmFlags
            )
        );
    }

    @LinstorData(
        tableName = "VOLUMES"
    )
    @JsonInclude(Include.NON_NULL)
    public static class VolumesSpec implements LinstorSpec<Volumes, VolumesSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -1470764504919505783L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable Volumes parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name") public final String nodeName; // PK
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("vlm_flags") public final long vlmFlags;

        @JsonIgnore
        public static VolumesSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new VolumesSpec(
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.NODE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.Volumes.VLM_FLAGS)
            );
        }

        @JsonCreator
        public VolumesSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name") String nodeNameRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("vlm_flags") long vlmFlagsRef
        )
        {
            uuid = uuidRef;
            nodeName = nodeNameRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            vlmNr = vlmNrRef;
            vlmFlags = vlmFlagsRef;

            formattedPrimaryKey = String.format(
                VolumesSpec.PK_FORMAT,
                nodeName,
                resourceName,
                snapshotName,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public Volumes getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new Volumes(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME", nodeName);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("VLM_NR", vlmNr);
            ret.put("VLM_FLAGS", vlmFlags);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME":
                    return nodeName;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "VLM_NR":
                    return vlmNr;
                case "VLM_FLAGS":
                    return vlmFlags;
                default:
                    throw new ImplementationError("Unknown database column. Table: VOLUMES, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.VOLUMES;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("volumes")
    @Singular("volumes")
    public static class Volumes extends CustomResource<VolumesSpec, Void> implements LinstorCrd<VolumesSpec>
    {
        private static final long serialVersionUID = -1432783678168245806L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public Volumes()
        {
        }

        public Volumes(VolumesSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(VolumesSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static VolumeConnections createVolumeConnections(
        String uuid,
        String nodeNameSrc,
        String nodeNameDst,
        String resourceName,
        String snapshotName,
        int vlmNr
    )
    {
        return new VolumeConnections(
            new VolumeConnectionsSpec(
                uuid,
                nodeNameSrc,
                nodeNameDst,
                resourceName,
                snapshotName,
                vlmNr
            )
        );
    }

    @LinstorData(
        tableName = "VOLUME_CONNECTIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class VolumeConnectionsSpec implements LinstorSpec<VolumeConnections, VolumeConnectionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 5017150339249070879L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%s:%s:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable VolumeConnections parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("node_name_src") public final String nodeNameSrc; // PK
        @JsonProperty("node_name_dst") public final String nodeNameDst; // PK
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK

        @JsonIgnore
        public static VolumeConnectionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new VolumeConnectionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.NODE_NAME_SRC),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.NODE_NAME_DST),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeConnections.VLM_NR)
            );
        }

        @JsonCreator
        public VolumeConnectionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("node_name_src") String nodeNameSrcRef,
            @JsonProperty("node_name_dst") String nodeNameDstRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("vlm_nr") int vlmNrRef
        )
        {
            uuid = uuidRef;
            nodeNameSrc = nodeNameSrcRef;
            nodeNameDst = nodeNameDstRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            vlmNr = vlmNrRef;

            formattedPrimaryKey = String.format(
                VolumeConnectionsSpec.PK_FORMAT,
                nodeNameSrc,
                nodeNameDst,
                resourceName,
                snapshotName,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public VolumeConnections getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new VolumeConnections(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("NODE_NAME_SRC", nodeNameSrc);
            ret.put("NODE_NAME_DST", nodeNameDst);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("VLM_NR", vlmNr);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "NODE_NAME_SRC":
                    return nodeNameSrc;
                case "NODE_NAME_DST":
                    return nodeNameDst;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "VLM_NR":
                    return vlmNr;
                default:
                    throw new ImplementationError("Unknown database column. Table: VOLUME_CONNECTIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.VOLUME_CONNECTIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("volumeconnections")
    @Singular("volumeconnections")
    public static class VolumeConnections extends CustomResource<VolumeConnectionsSpec, Void> implements LinstorCrd<VolumeConnectionsSpec>
    {
        private static final long serialVersionUID = 4287468674834216006L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public VolumeConnections()
        {
        }

        public VolumeConnections(VolumeConnectionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(VolumeConnectionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static VolumeDefinitions createVolumeDefinitions(
        String uuid,
        String resourceName,
        String snapshotName,
        int vlmNr,
        long vlmSize,
        long vlmFlags
    )
    {
        return new VolumeDefinitions(
            new VolumeDefinitionsSpec(
                uuid,
                resourceName,
                snapshotName,
                vlmNr,
                vlmSize,
                vlmFlags
            )
        );
    }

    @LinstorData(
        tableName = "VOLUME_DEFINITIONS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class VolumeDefinitionsSpec implements LinstorSpec<VolumeDefinitions, VolumeDefinitionsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = -8284292585757868330L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%s:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable VolumeDefinitions parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("resource_name") public final String resourceName; // PK
        @JsonProperty("snapshot_name") public final String snapshotName; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("vlm_size") public final long vlmSize;
        @JsonProperty("vlm_flags") public final long vlmFlags;

        @JsonIgnore
        public static VolumeDefinitionsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new VolumeDefinitionsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.RESOURCE_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.SNAPSHOT_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_SIZE),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeDefinitions.VLM_FLAGS)
            );
        }

        @JsonCreator
        public VolumeDefinitionsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("resource_name") String resourceNameRef,
            @JsonProperty("snapshot_name") String snapshotNameRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("vlm_size") long vlmSizeRef,
            @JsonProperty("vlm_flags") long vlmFlagsRef
        )
        {
            uuid = uuidRef;
            resourceName = resourceNameRef;
            snapshotName = snapshotNameRef;
            vlmNr = vlmNrRef;
            vlmSize = vlmSizeRef;
            vlmFlags = vlmFlagsRef;

            formattedPrimaryKey = String.format(
                VolumeDefinitionsSpec.PK_FORMAT,
                resourceName,
                snapshotName,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public VolumeDefinitions getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new VolumeDefinitions(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("RESOURCE_NAME", resourceName);
            ret.put("SNAPSHOT_NAME", snapshotName);
            ret.put("VLM_NR", vlmNr);
            ret.put("VLM_SIZE", vlmSize);
            ret.put("VLM_FLAGS", vlmFlags);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "RESOURCE_NAME":
                    return resourceName;
                case "SNAPSHOT_NAME":
                    return snapshotName;
                case "VLM_NR":
                    return vlmNr;
                case "VLM_SIZE":
                    return vlmSize;
                case "VLM_FLAGS":
                    return vlmFlags;
                default:
                    throw new ImplementationError("Unknown database column. Table: VOLUME_DEFINITIONS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.VOLUME_DEFINITIONS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("volumedefinitions")
    @Singular("volumedefinitions")
    public static class VolumeDefinitions extends CustomResource<VolumeDefinitionsSpec, Void> implements LinstorCrd<VolumeDefinitionsSpec>
    {
        private static final long serialVersionUID = 8389824368983039349L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public VolumeDefinitions()
        {
        }

        public VolumeDefinitions(VolumeDefinitionsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(VolumeDefinitionsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static VolumeGroups createVolumeGroups(
        String uuid,
        String resourceGroupName,
        int vlmNr,
        long flags
    )
    {
        return new VolumeGroups(
            new VolumeGroupsSpec(
                uuid,
                resourceGroupName,
                vlmNr,
                flags
            )
        );
    }

    @LinstorData(
        tableName = "VOLUME_GROUPS"
    )
    @JsonInclude(Include.NON_NULL)
    public static class VolumeGroupsSpec implements LinstorSpec<VolumeGroups, VolumeGroupsSpec>
    {
        @JsonIgnore private static final long serialVersionUID = 4835118944219851451L;
        @JsonIgnore private static final String PK_FORMAT = "%s:%d";

        @JsonIgnore private final String formattedPrimaryKey;
        @JsonIgnore private @Nullable VolumeGroups parentCrd;

        @JsonProperty("uuid") public final String uuid;
        @JsonProperty("resource_group_name") public final String resourceGroupName; // PK
        @JsonProperty("vlm_nr") public final int vlmNr; // PK
        @JsonProperty("flags") public final long flags;

        @JsonIgnore
        public static VolumeGroupsSpec fromRawParameters(RawParameters rawParamsRef)
        {
            return new VolumeGroupsSpec(
                rawParamsRef.get(GeneratedDatabaseTables.VolumeGroups.UUID),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeGroups.RESOURCE_GROUP_NAME),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeGroups.VLM_NR),
                rawParamsRef.get(GeneratedDatabaseTables.VolumeGroups.FLAGS)
            );
        }

        @JsonCreator
        public VolumeGroupsSpec(
            @JsonProperty("uuid") String uuidRef,
            @JsonProperty("resource_group_name") String resourceGroupNameRef,
            @JsonProperty("vlm_nr") int vlmNrRef,
            @JsonProperty("flags") long flagsRef
        )
        {
            uuid = uuidRef;
            resourceGroupName = resourceGroupNameRef;
            vlmNr = vlmNrRef;
            flags = flagsRef;

            formattedPrimaryKey = String.format(
                VolumeGroupsSpec.PK_FORMAT,
                resourceGroupName,
                vlmNr
            );
        }

        @JsonIgnore
        @Override
        public VolumeGroups getCrd()
        {
            if (parentCrd == null)
            {
                parentCrd = new VolumeGroups(this);
            }
            return parentCrd;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> asRawParameters()
        {
            Map<String, Object> ret = new TreeMap<>();
            ret.put("UUID", uuid);
            ret.put("RESOURCE_GROUP_NAME", resourceGroupName);
            ret.put("VLM_NR", vlmNr);
            ret.put("FLAGS", flags);
            return ret;
        }

        @JsonIgnore
        @Override
        @Nullable
        public Object getByColumn(String clmNameStr)
        {
            switch (clmNameStr)
            {
                case "UUID":
                    return uuid;
                case "RESOURCE_GROUP_NAME":
                    return resourceGroupName;
                case "VLM_NR":
                    return vlmNr;
                case "FLAGS":
                    return flags;
                default:
                    throw new ImplementationError("Unknown database column. Table: VOLUME_GROUPS, Column: " + clmNameStr);
            }
        }

        @JsonIgnore
        @Override
        public final String getLinstorKey()
        {
            return formattedPrimaryKey;
        }

        @Override
        @JsonIgnore
        public DatabaseTable getDatabaseTable()
        {
            return GeneratedDatabaseTables.VOLUME_GROUPS;
        }
    }

    @Version(GenCrdCurrent.VERSION)
    @Group(GenCrdCurrent.GROUP)
    @Plural("volumegroups")
    @Singular("volumegroups")
    public static class VolumeGroups extends CustomResource<VolumeGroupsSpec, Void> implements LinstorCrd<VolumeGroupsSpec>
    {
        private static final long serialVersionUID = -502660975876984750L;
        @Nullable String k8sKey = null;

        @JsonCreator
        public VolumeGroups()
        {
        }

        public VolumeGroups(VolumeGroupsSpec spec)
        {
            setMetadata(new ObjectMetaBuilder().withName(deriveKey(spec.getLinstorKey())).build());
            setSpec(spec);
        }

        @Override
        public void setSpec(VolumeGroupsSpec specRef)
        {
            super.setSpec(specRef);
            spec.parentCrd = this;
        }

        @Override
        public void setMetadata(ObjectMeta metadataRef)
        {
            super.setMetadata(metadataRef);
            k8sKey = metadataRef.getName();
        }

        @Override
        @JsonIgnore
        public String getLinstorKey()
        {
            return spec.getLinstorKey();
        }

        @Override
        @JsonIgnore
        public @Nullable String getK8sKey()
        {
            return k8sKey;
        }
    }

    public static final String deriveKey(String formattedPrimaryKey)
    {
        String sha = KEY_LUT.get(formattedPrimaryKey);
        if (sha == null)
        {
            synchronized (KEY_LUT)
            {
                sha = KEY_LUT.get(formattedPrimaryKey);
                if (sha == null)
                {
                    sha = ByteUtils.bytesToHex(ByteUtils.checksumSha256(formattedPrimaryKey.getBytes(StandardCharsets.UTF_8))).toLowerCase();
                    while (!USED_K8S_KEYS.add(sha))
                    {
                        String modifiedPk = formattedPrimaryKey + NEXT_ID.incrementAndGet();
                        sha = ByteUtils.bytesToHex(ByteUtils.checksumSha256(modifiedPk.getBytes(StandardCharsets.UTF_8))).toLowerCase();
                    }
                    KEY_LUT.put(formattedPrimaryKey, sha);
                }
            }
        }
        return sha;
    }

    public static class JsonTypeResolver extends TypeIdResolverBase
    {
        private @Nullable JavaType baseType;

        @Override
        public void init(JavaType baseTypeRef)
        {
            super.init(baseTypeRef);
            baseType = baseTypeRef;
        }

        @Override
        public String idFromValue(Object valueRef)
        {
            return idFromValueAndType(valueRef, valueRef.getClass());
        }

        @Override
        public String idFromValueAndType(Object ignored, Class<?> suggestedTypeRef)
        {
            return suggestedTypeRef.getSimpleName();
        }

        @Override
        public Id getMechanism()
        {
            return Id.MINIMAL_CLASS;
        }

        @Override
        public JavaType typeFromId(DatabindContext contextRef, String idRef)
        {
            Class<?> typeClass = JSON_ID_TO_TYPE_CLASS_LUT.get(idRef);
            return TypeFactory.defaultInstance().constructSpecializedType(baseType, typeClass);
        }
    }
}
