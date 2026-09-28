package com.linbit.linstor.api.interfaces;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.pojo.BCacheRscPojo.BCacheVlmPojo;
import com.linbit.linstor.api.pojo.CacheRscPojo.CacheVlmPojo;
import com.linbit.linstor.api.pojo.DrbdRscPojo.DrbdVlmPojo;
import com.linbit.linstor.api.pojo.LuksRscPojo.LuksVlmPojo;
import com.linbit.linstor.api.pojo.NvmeRscPojo.NvmeVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.DisklessVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.EbsVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.FileThinVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.FileVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.LvmThinVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.LvmVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.SpdkVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.ZfsThinVlmPojo;
import com.linbit.linstor.api.pojo.StorageRscPojo.ZfsVlmPojo;
import com.linbit.linstor.api.pojo.WritecacheRscPojo.WritecacheVlmPojo;
import com.linbit.linstor.core.apis.StorPoolApi;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind"
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = DisklessVlmPojo.class, name = "diskless"),
        @JsonSubTypes.Type(value = LvmVlmPojo.class, name = "lvm"),
        @JsonSubTypes.Type(value = SpdkVlmPojo.class, name = "spdk"),
        @JsonSubTypes.Type(value = LvmThinVlmPojo.class, name = "lvmThin"),
        @JsonSubTypes.Type(value = ZfsVlmPojo.class, name = "zfs"),
        @JsonSubTypes.Type(value = ZfsThinVlmPojo.class, name = "zfsThin"),
        @JsonSubTypes.Type(value = FileVlmPojo.class, name = "file"),
        @JsonSubTypes.Type(value = FileThinVlmPojo.class, name = "fileThin"),
        @JsonSubTypes.Type(value = CacheVlmPojo.class, name = "cache"),
        @JsonSubTypes.Type(value = DrbdVlmPojo.class, name = "drbd"),
        @JsonSubTypes.Type(value = LuksVlmPojo.class, name = "luks"),
        @JsonSubTypes.Type(value = NvmeVlmPojo.class, name = "nvme"),
        @JsonSubTypes.Type(value = WritecacheVlmPojo.class, name = "writecache"),
        @JsonSubTypes.Type(value = BCacheVlmPojo.class, name = "bcache"),
        @JsonSubTypes.Type(value = EbsVlmPojo.class, name = "ebs")
    // remoteSPDK is missing as we cannot ship backups from it
    // since we have no direct access to the snapshots
    }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface VlmLayerDataApi
{
    int getVlmNr();

    @JsonIgnore
    DeviceLayerKind getLayerKind();

    @JsonIgnore
    DeviceProviderKind getProviderKind();

    @JsonIgnore
    String getDevicePath();

    long getAllocatedSize();

    long getUsableSize();

    @JsonIgnore
    long getDiscGran();

    @JsonIgnore
    boolean exists();

    default @Nullable Long getSnapshotAllocatedSize()
    {
        return null;
    }

    default @Nullable Long getSnapshotUsableSize()
    {
        return null;
    }

    @JsonIgnore
    String getDiskState();

    default @Nullable StorPoolApi getStorPoolApi()
    {
        return null; // layers should not have storage pools (only storage layer / diskless vlm)
    }

    default @Nullable Long getExtentSize()
    {
        return null;
    }
}
