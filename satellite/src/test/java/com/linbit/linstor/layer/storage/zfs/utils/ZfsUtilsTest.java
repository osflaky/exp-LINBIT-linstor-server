package com.linbit.linstor.layer.storage.zfs.utils;

import com.linbit.linstor.layer.storage.zfs.utils.ZfsCommands.ZfsVolumeType;
import com.linbit.linstor.storage.StorageException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ZfsUtilsTest
{
    @Test
    public void zPoolRootNameOfNestedDataset()
    {
        assertThat(ZfsUtils.getZPoolRootName("rpool/linstor/rsc0_00000")).isEqualTo("rpool");
        assertThat(ZfsUtils.getZPoolRootName("tank/vol")).isEqualTo("tank");
    }

    @Test
    public void zPoolRootNameOfRootDataset()
    {
        assertThat(ZfsUtils.getZPoolRootName("rpool")).isEqualTo("rpool");
        assertThat(ZfsUtils.getZPoolRootName("")).isEqualTo("");
    }

    @Test
    public void zfsVolumeTypeParseNullable()
    {
        assertThat(ZfsVolumeType.parseNullable("volume")).isEqualTo(ZfsVolumeType.VOLUME);
        assertThat(ZfsVolumeType.parseNullable("snapshot")).isEqualTo(ZfsVolumeType.SNAPSHOT);
        assertThat(ZfsVolumeType.parseNullable("filesystem")).isEqualTo(ZfsVolumeType.FILESYSTEM);
        // parsing ignores case
        assertThat(ZfsVolumeType.parseNullable("VOLUME")).isEqualTo(ZfsVolumeType.VOLUME);
        assertThat(ZfsVolumeType.parseNullable("bogus")).isNull();
    }

    @Test
    public void zfsVolumeTypeParseOrThrow() throws StorageException
    {
        assertThat(ZfsVolumeType.parseOrThrow("volume")).isEqualTo(ZfsVolumeType.VOLUME);

        assertThatThrownBy(() -> ZfsVolumeType.parseOrThrow("bogus"))
            .isInstanceOf(StorageException.class)
            .hasMessageContaining("bogus");
    }

    @Test
    public void zfsVolumeTypeDescr()
    {
        assertThat(ZfsVolumeType.VOLUME.getDescr()).isEqualTo("volume");
        assertThat(ZfsVolumeType.SNAPSHOT.getDescr()).isEqualTo("snapshot");
        assertThat(ZfsVolumeType.FILESYSTEM.getDescr()).isEqualTo("filesystem");
    }
}
