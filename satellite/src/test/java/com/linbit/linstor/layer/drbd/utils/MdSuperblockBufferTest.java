package com.linbit.linstor.layer.drbd.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests reading DRBD meta data superblocks from a backing object.
 *
 * The ExtCmdFactory parameter of readObject/wipe is only used on Windows, so the tests
 * can pass null and work on plain temporary files.
 */
public class MdSuperblockBufferTest
{
    private static final int SUPERBLK_SIZE = 4096;
    private static final int DRBD_MAGIC_ID = 0x8374026D;

    private static final long EFFECTIVE_SIZE = 8_388_608L;
    private static final long CURRENT_GEN = 0x123456789ABCDEFL;
    private static final long DEVICE_GEN = 0xFEDCBA987654321L;
    private static final int FLAGS = 0x13;
    private static final int MD_SIZE = 2048;
    private static final int AL_OFFSET = -64;
    private static final int AL_EXTENTS = 1237;
    private static final int BITMAP_OFFSET = -1024;
    private static final int BITMAP_BIT_BLOCKSIZE = 12;
    private static final int MAX_BIO_SIZE = 1 << 20;
    private static final int MAX_PEERS = 7;
    private static final int NODE_ID = 3;
    private static final int AL_STRIPES = 1;
    private static final int AL_STRIPE_SIZE = 32;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static byte[] superblock(long currentGen, int magic)
    {
        ByteBuffer buffer = ByteBuffer.allocate(SUPERBLK_SIZE);
        buffer.putLong(EFFECTIVE_SIZE);
        buffer.putLong(currentGen);
        buffer.putLong(0L); // reserved
        buffer.putLong(0L); // reserved
        buffer.putLong(0L); // reserved
        buffer.putLong(0L); // reserved
        buffer.putLong(DEVICE_GEN);
        buffer.putInt(FLAGS);
        buffer.putInt(magic);
        buffer.putInt(MD_SIZE);
        buffer.putInt(AL_OFFSET);
        buffer.putInt(AL_EXTENTS);
        buffer.putInt(BITMAP_OFFSET);
        buffer.putInt(BITMAP_BIT_BLOCKSIZE);
        buffer.putInt(MAX_BIO_SIZE);
        buffer.putInt(MAX_PEERS);
        buffer.putInt(NODE_ID);
        buffer.putInt(AL_STRIPES);
        buffer.putInt(AL_STRIPE_SIZE);
        return buffer.array();
    }

    private File createBackingFile(long fileSize, long superblockOffset, byte[] superblockData)
        throws IOException
    {
        File backingFile = tempFolder.newFile();
        try (RandomAccessFile file = new RandomAccessFile(backingFile, "rw"))
        {
            file.setLength(fileSize);
            file.seek(superblockOffset);
            file.write(superblockData);
        }
        return backingFile;
    }

    @Test
    public void readInternalMetaData() throws IOException
    {
        // internal meta data: superblock sits in the last 4k of the device
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            SUPERBLK_SIZE,
            superblock(CURRENT_GEN, DRBD_MAGIC_ID)
        );

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);

        assertThat(superblock.hasMetaData()).isTrue();
        assertThat(superblock.isMetaDataNew()).isFalse();
        assertThat(superblock.getEffectiveSize()).isEqualTo(EFFECTIVE_SIZE);
        assertThat(superblock.getCurrentGen()).isEqualTo(CURRENT_GEN);
        assertThat(superblock.getDeviceGen()).isEqualTo(DEVICE_GEN);
        assertThat(superblock.getFlags()).isEqualTo(FLAGS);
        assertThat(superblock.getMagic()).isEqualTo(DRBD_MAGIC_ID);
        assertThat(superblock.getSize()).isEqualTo(MD_SIZE);
        assertThat(superblock.getAlOffset()).isEqualTo(AL_OFFSET);
        assertThat(superblock.getAlExtents()).isEqualTo(AL_EXTENTS);
        assertThat(superblock.getBitmapOffset()).isEqualTo(BITMAP_OFFSET);
        assertThat(superblock.getBitmapBitBlocksize()).isEqualTo(BITMAP_BIT_BLOCKSIZE);
        assertThat(superblock.getMaxBioSize()).isEqualTo(MAX_BIO_SIZE);
        assertThat(superblock.getMaxPeers()).isEqualTo(MAX_PEERS);
        assertThat(superblock.getNodeId()).isEqualTo(NODE_ID);
        assertThat(superblock.getAlStripes()).isEqualTo(AL_STRIPES);
        assertThat(superblock.getAlStripeSize()).isEqualTo(AL_STRIPE_SIZE);
    }

    @Test
    public void readExternalMetaData() throws IOException
    {
        // external meta data: superblock sits at the start of the object
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            0,
            superblock(CURRENT_GEN, DRBD_MAGIC_ID)
        );

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), true);

        assertThat(superblock.hasMetaData()).isTrue();
        assertThat(superblock.getEffectiveSize()).isEqualTo(EFFECTIVE_SIZE);
    }

    @Test
    public void wrongMagicMeansNoMetaData() throws IOException
    {
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            SUPERBLK_SIZE,
            superblock(CURRENT_GEN, 0xCAFEBABE)
        );

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);

        assertThat(superblock.hasMetaData()).isFalse();
    }

    @Test
    public void zeroedDeviceHasNoMetaData() throws IOException
    {
        File backingFile = createBackingFile(2 * SUPERBLK_SIZE, SUPERBLK_SIZE, new byte[SUPERBLK_SIZE]);

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);

        assertThat(superblock.hasMetaData()).isFalse();
        assertThat(superblock.isMetaDataNew()).isTrue();
    }

    @Test
    public void initialGenerationCountsAsNewMetaData() throws IOException
    {
        // a freshly created meta data set starts with a current generation of 0x0 or 0x4
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            SUPERBLK_SIZE,
            superblock(0x4L, DRBD_MAGIC_ID)
        );

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);

        assertThat(superblock.hasMetaData()).isTrue();
        assertThat(superblock.isMetaDataNew()).isTrue();
    }

    @Test
    public void clearResetsAllFields() throws IOException
    {
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            SUPERBLK_SIZE,
            superblock(CURRENT_GEN, DRBD_MAGIC_ID)
        );

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);
        superblock.clear();

        assertThat(superblock.hasMetaData()).isFalse();
        assertThat(superblock.getEffectiveSize()).isZero();
        assertThat(superblock.getCurrentGen()).isZero();
        assertThat(superblock.getMagic()).isZero();
        assertThat(superblock.getMaxPeers()).isZero();
    }

    @Test
    public void wipeRemovesInternalMetaData() throws IOException
    {
        File backingFile = createBackingFile(
            2 * SUPERBLK_SIZE,
            SUPERBLK_SIZE,
            superblock(CURRENT_GEN, DRBD_MAGIC_ID)
        );

        MdSuperblockBuffer.wipe(null, backingFile.getPath(), false);

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        superblock.readObject(null, backingFile.getPath(), false);
        assertThat(superblock.hasMetaData()).isFalse();
    }

    @Test
    public void tooSmallObjectThrows() throws IOException
    {
        File backingFile = tempFolder.newFile();
        try (RandomAccessFile file = new RandomAccessFile(backingFile, "rw"))
        {
            file.setLength(SUPERBLK_SIZE - 1);
        }

        MdSuperblockBuffer superblock = new MdSuperblockBuffer();
        assertThatThrownBy(() -> superblock.readObject(null, backingFile.getPath(), false))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("too small");
    }
}
