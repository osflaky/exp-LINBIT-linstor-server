package com.linbit.linstor;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.identifier.ExternalFileName;
import com.linbit.linstor.core.identifier.KeyValueStoreName;
import com.linbit.linstor.core.identifier.NetInterfaceName;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.identifier.RemoteName;
import com.linbit.linstor.core.identifier.ResourceGroupName;
import com.linbit.linstor.core.identifier.ResourceName;
import com.linbit.linstor.core.identifier.ScheduleName;
import com.linbit.linstor.core.identifier.SharedStorPoolName;
import com.linbit.linstor.core.identifier.SnapshotName;
import com.linbit.linstor.core.identifier.StorPoolName;
import com.linbit.linstor.core.identifier.VolumeNumber;
import com.linbit.linstor.core.objects.NetInterface.EncryptionType;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.types.LsIpAddress;
import com.linbit.linstor.core.types.TcpPortNumber;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;
import com.linbit.linstor.storage.kinds.RaidLevel;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.event.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class LinstorParsingUtilsTest
{
    /**
     * Runs the given callable, expects an {@link ApiRcException} with exactly one entry and returns that entry.
     */
    private static ApiCallRc.RcEntry catchSingleEntry(ThrowingCallable callable)
    {
        ApiRcException apiRcExc = catchThrowableOfType(ApiRcException.class, callable);
        assertThat(apiRcExc).isNotNull();
        assertThat(apiRcExc.getApiCallRc()).hasSize(1);
        return apiRcExc.getApiCallRc().get(0);
    }

    @Test
    public void asNodeNameValid()
    {
        NodeName nodeName = LinstorParsingUtils.asNodeName("Node-1.example");
        assertThat(nodeName.displayValue).isEqualTo("Node-1.example");
        assertThat(nodeName.value).isEqualTo("NODE-1.EXAMPLE");
    }

    @Test
    public void asNodeNameInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asNodeName("-leadingdash"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NODE_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given node name '-leadingdash' is invalid.");

        // too short (host names need at least 2 characters)
        entry = catchSingleEntry(() -> LinstorParsingUtils.asNodeName("a"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NODE_NAME);
    }

    @Test
    public void asNodeTypeValid()
    {
        assertThat(LinstorParsingUtils.asNodeType("satellite")).isEqualTo(Node.Type.SATELLITE);
        assertThat(LinstorParsingUtils.asNodeType("Controller")).isEqualTo(Node.Type.CONTROLLER);
        assertThat(LinstorParsingUtils.asNodeType("COMBINED")).isEqualTo(Node.Type.COMBINED);
        assertThat(LinstorParsingUtils.asNodeType("auxiliary")).isEqualTo(Node.Type.AUXILIARY);
    }

    @Test
    public void asNodeTypeNullFallsBackToSatellite()
    {
        assertThat(LinstorParsingUtils.asNodeType(null)).isEqualTo(Node.Type.SATELLITE);
    }

    @Test
    public void asNodeTypeInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asNodeType("quantum"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NODE_TYPE);
        assertThat(entry.getMessage()).isEqualTo("The specified node type 'quantum' is invalid.");
        assertThat(entry.getCorrection())
            .contains("CONTROLLER")
            .contains("SATELLITE")
            .contains("COMBINED")
            .contains("AUXILIARY");
    }

    @Test
    public void asEncryptionTypeValid()
    {
        assertThat(LinstorParsingUtils.asEncryptionType("ssl")).isEqualTo(EncryptionType.SSL);
        assertThat(LinstorParsingUtils.asEncryptionType("Plain")).isEqualTo(EncryptionType.PLAIN);
    }

    @Test
    public void asEncryptionTypeInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asEncryptionType("rot13"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_TYPE);
        assertThat(entry.getMessage()).isEqualTo("The given encryption type 'rot13' is invalid.");

        // null is caught by the generic catch clause and reported the same way
        entry = catchSingleEntry(() -> LinstorParsingUtils.asEncryptionType(null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_TYPE);
    }

    @Test
    public void asNetInterfaceNameValid()
    {
        NetInterfaceName netIfName = LinstorParsingUtils.asNetInterfaceName("eth0");
        assertThat(netIfName.displayValue).isEqualTo("eth0");
    }

    @Test
    public void asNetInterfaceNameInvalid()
    {
        // minimum length for net interface names is 3
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asNetInterfaceName("e"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_NAME);
        assertThat(entry.getMessage()).isEqualTo("The specified net interface name 'e' is invalid.");
    }

    @Test
    public void asLsIpAddressValid()
    {
        LsIpAddress ipv4 = LinstorParsingUtils.asLsIpAddress("10.0.0.1");
        assertThat(ipv4.getAddress()).isEqualTo("10.0.0.1");
        assertThat(ipv4.getAddressType()).isEqualTo(LsIpAddress.AddrType.IPv4);

        // IPv6 addresses are trimmed and upper-cased
        LsIpAddress ipv6 = LinstorParsingUtils.asLsIpAddress(" fe80::1 ");
        assertThat(ipv6.getAddress()).isEqualTo("FE80::1");
        assertThat(ipv6.getAddressType()).isEqualTo(LsIpAddress.AddrType.IPv6);
    }

    @Test
    public void asLsIpAddressInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asLsIpAddress("not.an.ip"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_ADDR);
        assertThat(entry.getMessage()).isEqualTo("Failed to parse IP address");
        assertThat(entry.getCause()).isEqualTo("The specified IP address is not valid");
        assertThat(entry.getDetails()).isEqualTo("The specified input 'not.an.ip' is not a valid IP address.");
        assertThat(entry.getCorrection()).isEqualTo("Specify a valid IPv4 or IPv6 address.");

        // null is explicitly handled via the caught NullPointerException
        entry = catchSingleEntry(() -> LinstorParsingUtils.asLsIpAddress(null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_ADDR);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void asTcpPortNumberValid()
    {
        assertThat(LinstorParsingUtils.asTcpPortNumber(TcpPortNumber.PORT_NR_MIN).value)
            .isEqualTo(TcpPortNumber.PORT_NR_MIN);
        assertThat(LinstorParsingUtils.asTcpPortNumber(TcpPortNumber.PORT_NR_MAX).value)
            .isEqualTo(65535);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void asTcpPortNumberInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asTcpPortNumber(0));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_PORT);
        assertThat(entry.getMessage()).isEqualTo("The given portNumber '0' is invalid.");

        entry = catchSingleEntry(() -> LinstorParsingUtils.asTcpPortNumber(65536));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_NET_PORT);
    }

    @Test
    public void asRscNameValid()
    {
        ResourceName rscName = LinstorParsingUtils.asRscName("rsc1");
        assertThat(rscName.displayValue).isEqualTo("rsc1");
        assertThat(rscName.value).isEqualTo("RSC1");

        // leading underscore is explicitly allowed
        assertThat(LinstorParsingUtils.asRscName("_rsc").displayValue).isEqualTo("_rsc");
    }

    @Test
    public void asRscNameInvalid()
    {
        // "all" and "proxy" are reserved DRBD keywords
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asRscName("all"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_RSC_NAME);
        assertThat(entry.getMessage()).isEqualTo("The specified resource name 'all' is invalid.");

        entry = catchSingleEntry(() -> LinstorParsingUtils.asRscName("Proxy"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_RSC_NAME);

        // resource names must not start with a digit
        entry = catchSingleEntry(() -> LinstorParsingUtils.asRscName("1abc"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_RSC_NAME);

        // too short
        entry = catchSingleEntry(() -> LinstorParsingUtils.asRscName("a"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_RSC_NAME);
    }

    @Test
    public void asVlmNrValid()
    {
        assertThat(LinstorParsingUtils.asVlmNr(VolumeNumber.VOLUME_NR_MIN).value)
            .isEqualTo(VolumeNumber.VOLUME_NR_MIN);
        assertThat(LinstorParsingUtils.asVlmNr(VolumeNumber.VOLUME_NR_MAX).value)
            .isEqualTo(VolumeNumber.VOLUME_NR_MAX);
    }

    @Test
    public void asVlmNrInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asVlmNr(VolumeNumber.VOLUME_NR_MIN - 1));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_VLM_NR);
        assertThat(entry.getMessage())
            .isEqualTo("The given volume number '-1' is invalid. Valid range from 0 to 65534");

        entry = catchSingleEntry(() -> LinstorParsingUtils.asVlmNr(VolumeNumber.VOLUME_NR_MAX + 1));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_VLM_NR);
    }

    @Test
    public void asStorPoolNameValid()
    {
        StorPoolName storPoolName = LinstorParsingUtils.asStorPoolName("pool1");
        assertThat(storPoolName.displayValue).isEqualTo("pool1");
        assertThat(storPoolName.value).isEqualTo("POOL1");
    }

    @Test
    public void asStorPoolNameInvalid()
    {
        // minimum length for storage pool names is 3
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asStorPoolName("ab"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_STOR_POOL_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given storage pool name 'ab' is invalid.");
        // default overload does not skip the error report
        assertThat(entry.skipErrorReport()).isFalse();
    }

    @Test
    public void asStorPoolNameInvalidWithSkipErrorReport()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asStorPoolName("ab", true));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_STOR_POOL_NAME);
        assertThat(entry.skipErrorReport()).isTrue();
    }

    @Test
    public void asSnapshotNameValid()
    {
        SnapshotName snapshotName = LinstorParsingUtils.asSnapshotName("snap1");
        assertThat(snapshotName.displayValue).isEqualTo("snap1");
    }

    @Test
    public void asSnapshotNameInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asSnapshotName("s"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_SNAPSHOT_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given snapshot name 's' is invalid.");
    }

    @Test
    public void asSharedStorPoolNameValid()
    {
        // shared storage pool names may even start with a digit
        SharedStorPoolName sharedStorPoolName = LinstorParsingUtils.asSharedStorPoolName("1pool");
        assertThat(sharedStorPoolName.displayValue).isEqualTo("1pool");
    }

    @Test
    public void asSharedStorPoolNameInvalid()
    {
        // at least one [a-zA-Z] character is required
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asSharedStorPoolName("123"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_FREE_SPACE_MGR_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given free space manager name '123' is invalid.");

        // ';' is a reserved connector character
        entry = catchSingleEntry(() -> LinstorParsingUtils.asSharedStorPoolName("node1;pool"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_FREE_SPACE_MGR_NAME);
    }

    @Test
    public void asKvsNameValid()
    {
        KeyValueStoreName kvsName = LinstorParsingUtils.asKvsName("kvs1");
        assertThat(kvsName.displayValue).isEqualTo("kvs1");
    }

    @Test
    public void asKvsNameInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asKvsName("k"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_KVS_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given keyValueStore name 'k' is invalid.");
    }

    @Test
    public void asDeviceLayerKindOrNullMapsAllAliases()
    {
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("drbd")).isEqualTo(DeviceLayerKind.DRBD);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("CRYPT")).isEqualTo(DeviceLayerKind.LUKS);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("crypt_setup")).isEqualTo(DeviceLayerKind.LUKS);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("luks")).isEqualTo(DeviceLayerKind.LUKS);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("storage")).isEqualTo(DeviceLayerKind.STORAGE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("lvm")).isEqualTo(DeviceLayerKind.STORAGE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("spdk")).isEqualTo(DeviceLayerKind.STORAGE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("zfs")).isEqualTo(DeviceLayerKind.STORAGE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("nvme")).isEqualTo(DeviceLayerKind.NVME);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("writecache")).isEqualTo(DeviceLayerKind.WRITECACHE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("cache")).isEqualTo(DeviceLayerKind.CACHE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("bcache")).isEqualTo(DeviceLayerKind.BCACHE);
        assertThat(LinstorParsingUtils.asDeviceLayerKindOrNull("floppy")).isNull();
    }

    @Test
    public void asDeviceLayerKindInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asDeviceLayerKind("floppy"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_LAYER_KIND);
        assertThat(entry.getMessage()).isEqualTo("Given layer kind 'floppy' is invalid");
    }

    @Test
    public void asDeviceLayerKindList()
    {
        List<DeviceLayerKind> kinds = LinstorParsingUtils.asDeviceLayerKind(Arrays.asList("drbd", "storage"));
        assertThat(kinds).containsExactly(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE);

        assertThat(LinstorParsingUtils.asDeviceLayerKind((List<String>) null)).isEmpty();

        assertThatThrownBy(() -> LinstorParsingUtils.asDeviceLayerKind(Arrays.asList("drbd", "floppy")))
            .isInstanceOf(ApiRcException.class);
    }

    @Test
    public void asProviderKindMapsAllAliases()
    {
        assertThat(LinstorParsingUtils.asProviderKind("drbd_diskless")).isEqualTo(DeviceProviderKind.DISKLESS);
        assertThat(LinstorParsingUtils.asProviderKind("drbddiskless")).isEqualTo(DeviceProviderKind.DISKLESS);
        assertThat(LinstorParsingUtils.asProviderKind("diskless")).isEqualTo(DeviceProviderKind.DISKLESS);
        assertThat(LinstorParsingUtils.asProviderKind("lvm")).isEqualTo(DeviceProviderKind.LVM);
        assertThat(LinstorParsingUtils.asProviderKind("lvmthin")).isEqualTo(DeviceProviderKind.LVM_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("lvm_thin")).isEqualTo(DeviceProviderKind.LVM_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("zfs")).isEqualTo(DeviceProviderKind.ZFS);
        assertThat(LinstorParsingUtils.asProviderKind("zfsthin")).isEqualTo(DeviceProviderKind.ZFS_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("zfs_thin")).isEqualTo(DeviceProviderKind.ZFS_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("file")).isEqualTo(DeviceProviderKind.FILE);
        assertThat(LinstorParsingUtils.asProviderKind("file_thin")).isEqualTo(DeviceProviderKind.FILE_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("spdk")).isEqualTo(DeviceProviderKind.SPDK);
        assertThat(LinstorParsingUtils.asProviderKind("remote_spdk")).isEqualTo(DeviceProviderKind.REMOTE_SPDK);
        assertThat(LinstorParsingUtils.asProviderKind("ebs_target")).isEqualTo(DeviceProviderKind.EBS_TARGET);
        assertThat(LinstorParsingUtils.asProviderKind("ebs_init")).isEqualTo(DeviceProviderKind.EBS_INIT);
        assertThat(LinstorParsingUtils.asProviderKind("storage_spaces"))
            .isEqualTo(DeviceProviderKind.STORAGE_SPACES);
        assertThat(LinstorParsingUtils.asProviderKind("storage_spaces_target"))
            .isEqualTo(DeviceProviderKind.STORAGE_SPACES);
        assertThat(LinstorParsingUtils.asProviderKind("storage_spaces_thin"))
            .isEqualTo(DeviceProviderKind.STORAGE_SPACES_THIN);
        assertThat(LinstorParsingUtils.asProviderKind("storage_spaces_thin_target"))
            .isEqualTo(DeviceProviderKind.STORAGE_SPACES_THIN);
    }

    @Test
    public void asProviderKindInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asProviderKind("punchcard"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_LAYER_KIND);
        assertThat(entry.getMessage()).isEqualTo("Given provider kind 'punchcard' is invalid");
    }

    @Test
    public void asProviderKindNull()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asProviderKind((String) null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_LAYER_KIND);
        assertThat(entry.getMessage()).isEqualTo("Given provider kind is null.");
    }

    @Test
    public void asProviderKindList()
    {
        List<DeviceProviderKind> kinds = LinstorParsingUtils.asProviderKind(Arrays.asList("lvm", "zfs_thin"));
        assertThat(kinds).containsExactly(DeviceProviderKind.LVM, DeviceProviderKind.ZFS_THIN);
    }

    @Test
    public void asRaidLevelValid()
    {
        assertThat(LinstorParsingUtils.asRaidLevel("jbod")).isEqualTo(RaidLevel.JBOD);
        assertThat(LinstorParsingUtils.asRaidLevel("JBOD")).isEqualTo(RaidLevel.JBOD);
    }

    @Test
    public void asRaidLevelNull()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asRaidLevel(null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_UNKNOWN_ERROR);
        assertThat(entry.getMessage()).isEqualTo("Given RAID level string is null.");
    }

    @Test
    public void asRaidLevelUnknownThrowsIllegalArgumentException()
    {
        // unknown levels are NOT wrapped into an ApiRcException but escape as IllegalArgumentException
        assertThatThrownBy(() -> LinstorParsingUtils.asRaidLevel("raid5"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void asRscGrpNameValid()
    {
        ResourceGroupName rscGrpName = LinstorParsingUtils.asRscGrpName("grp1");
        assertThat(rscGrpName.displayValue).isEqualTo("grp1");
    }

    @Test
    public void asRscGrpNameInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asRscGrpName("g"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_RSC_GRP_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given resource group name 'g' is invalid.");
    }

    @Test
    public void asRemoteNameValid()
    {
        RemoteName remoteName = LinstorParsingUtils.asRemoteName("remote1");
        assertThat(remoteName.displayValue).isEqualTo("remote1");
    }

    @Test
    public void asRemoteNameInvalid()
    {
        // public remote names are validated as host names, which must not start with '.'
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asRemoteName(".hidden"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_REMOTE_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given remote name '.hidden' is invalid.");
    }

    @Test
    public void asScheduleNameValid()
    {
        ScheduleName scheduleName = LinstorParsingUtils.asScheduleName("sched1");
        assertThat(scheduleName.displayValue).isEqualTo("sched1");
    }

    @Test
    public void asScheduleNameInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asScheduleName("-bad"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_SCHEDULE_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given schedule name '-bad' is invalid.");
    }

    @Test
    public void asLogLevelValid()
    {
        assertThat(LinstorParsingUtils.asLogLevel("error")).isEqualTo(Level.ERROR);
        assertThat(LinstorParsingUtils.asLogLevel("WARN")).isEqualTo(Level.WARN);
        assertThat(LinstorParsingUtils.asLogLevel("Info")).isEqualTo(Level.INFO);
        assertThat(LinstorParsingUtils.asLogLevel("debug")).isEqualTo(Level.DEBUG);
        assertThat(LinstorParsingUtils.asLogLevel("TRACE")).isEqualTo(Level.TRACE);
    }

    @Test
    public void asLogLevelNullOrEmpty()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asLogLevel(null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_CONF);
        assertThat(entry.getMessage()).isEqualTo("Given loglevel is null.");

        entry = catchSingleEntry(() -> LinstorParsingUtils.asLogLevel(""));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_CONF);
        assertThat(entry.getMessage()).isEqualTo("Given loglevel is null.");
    }

    @Test
    public void asLogLevelInvalid()
    {
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asLogLevel("verbose"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_CONF);
        assertThat(entry.getMessage()).isEqualTo("Given loglevel 'verbose' is invalid");
    }

    @Test
    public void asExtFileNameValid()
    {
        ExternalFileName extFileName = LinstorParsingUtils.asExtFileName("/etc/linstor/file.conf");
        assertThat(extFileName.extFileName).isEqualTo("/etc/linstor/file.conf");
    }

    @Test
    public void asExtFileNameInvalid()
    {
        // path must be absolute
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asExtFileName("etc/relative.conf"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_EXT_FILE_NAME);
        assertThat(entry.getMessage()).isEqualTo("The given external file name 'etc/relative.conf' is invalid.");

        // null is rejected as well
        entry = catchSingleEntry(() -> LinstorParsingUtils.asExtFileName(null));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_EXT_FILE_NAME);
    }

    @Test
    public void asExtFileNameNonAsciiInvalid()
    {
        // the ASCII check of ExternalFileName is only reachable on JVMs whose file system charset
        // can represent the characters at all: with a non-UTF-8 locale (e.g. POSIX in CI),
        // Paths.get already throws an unwrapped InvalidPathException before the check is reached
        Assume.assumeTrue("UTF-8".equalsIgnoreCase(System.getProperty("sun.jnu.encoding")));

        // path must be pure ASCII ("föö" = "foo" with umlauts, escaped to be independent
        // of the compiler's source file encoding)
        ApiCallRc.RcEntry entry = catchSingleEntry(() -> LinstorParsingUtils.asExtFileName("/etc/f\u00f6\u00f6"));
        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_INVLD_EXT_FILE_NAME);
    }
}
