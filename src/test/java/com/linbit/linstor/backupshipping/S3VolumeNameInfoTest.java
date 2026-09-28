package com.linbit.linstor.backupshipping;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class S3VolumeNameInfoTest
{
    private static final LocalDateTime BACKUP_TIME = LocalDateTime.of(2024, 3, 15, 12, 34, 56);

    @Test
    public void parseSimpleVolumeName() throws ParseException
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc_00000_back_20240315_123456");

        assertThat(info.rscName).isEqualTo("rsc");
        assertThat(info.layerSuffix).isEmpty();
        assertThat(info.vlmNr).isZero();
        assertThat(info.backupId).isEqualTo("back_20240315_123456");
        assertThat(info.backupTime).isEqualTo(BACKUP_TIME);
        assertThat(info.s3Suffix).isEmpty();
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
    }

    @Test
    public void parseWithLayerSuffix() throws ParseException
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc.ext_00001_back_20240315_123456");

        assertThat(info.rscName).isEqualTo("rsc");
        assertThat(info.layerSuffix).isEqualTo(".ext");
        assertThat(info.vlmNr).isEqualTo(1);
    }

    @Test
    public void parseWithS3Suffix() throws ParseException
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc_00001_back_20240315_123456:sfx");

        assertThat(info.s3Suffix).isEqualTo(":sfx");
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
    }

    @Test
    public void parseWithS3SuffixAndSnapName() throws ParseException
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc_00001_back_20240315_123456:sfx^snap");

        assertThat(info.s3Suffix).isEqualTo(":sfx");
        assertThat(info.snapName).isEqualTo("snap");
    }

    @Test
    public void parseSnapNameWithoutS3SuffixLandsInS3Suffix() throws ParseException
    {
        // characterization: unlike the meta file pattern, the volume pattern does not prioritize
        // an empty s3Suffix group, so a trailing "^snap" without s3 suffix is captured as s3Suffix
        // and the snapName falls back to the backupId
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc_00001_back_20240315_123456^snap");

        assertThat(info.s3Suffix).isEqualTo("^snap");
        assertThat(info.snapName).isEqualTo("back_20240315_123456");

        // the string representation is nevertheless stable
        assertThat(info.toString()).isEqualTo("rsc_00001_back_20240315_123456^snap");
    }

    @Test
    public void parseInvalidNamesThrowParseException()
    {
        // volume number must have exactly 5 digits
        ParseException parseExc = catchThrowableOfType(
            ParseException.class,
            () -> new S3VolumeNameInfo("rsc_1_back_20240315_123456")
        );
        assertThat(parseExc)
            .hasMessage("Failed to parse rsc_1_back_20240315_123456 as S3 backup meta file");
        assertThat(parseExc.getErrorOffset()).isZero();

        // rscName needs at least 2 characters
        assertThatThrownBy(() -> new S3VolumeNameInfo("r_00001_back_20240315_123456"))
            .isInstanceOf(ParseException.class);

        // date part needs exactly 8 digits
        assertThatThrownBy(() -> new S3VolumeNameInfo("rsc_00001_back_2024_123456"))
            .isInstanceOf(ParseException.class);

        assertThatThrownBy(() -> new S3VolumeNameInfo(""))
            .isInstanceOf(ParseException.class);
    }

    @Test
    public void buildFromPartsWithDefaults()
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc", null, 0, BACKUP_TIME, null, null);

        assertThat(info.layerSuffix).isEmpty();
        assertThat(info.backupId).isEqualTo("back_20240315_123456");
        assertThat(info.s3Suffix).isEmpty();
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
        // volume number is zero padded to 5 digits
        assertThat(info.toString()).isEqualTo("rsc_00000_back_20240315_123456");
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void buildFromPartsWithAllParts()
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc", ".ext", 7, BACKUP_TIME, ":sfx", "snap");

        assertThat(info.toString()).isEqualTo("rsc.ext_00007_back_20240315_123456:sfx^snap");
    }

    @Test
    public void buildFromPartsSnapNameEqualToBackupIdIsOmitted()
    {
        S3VolumeNameInfo info = new S3VolumeNameInfo("rsc", "", 0, BACKUP_TIME, "", "back_20240315_123456");
        assertThat(info.toString()).isEqualTo("rsc_00000_back_20240315_123456");
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void roundTripKeepsAllFields() throws ParseException
    {
        S3VolumeNameInfo original = new S3VolumeNameInfo("my-rsc", ".ext", 42, BACKUP_TIME, ":sfx", "snap");
        S3VolumeNameInfo reparsed = new S3VolumeNameInfo(original.toString());

        assertThat(reparsed.rscName).isEqualTo(original.rscName);
        assertThat(reparsed.layerSuffix).isEqualTo(original.layerSuffix);
        assertThat(reparsed.vlmNr).isEqualTo(original.vlmNr);
        assertThat(reparsed.backupId).isEqualTo(original.backupId);
        assertThat(reparsed.backupTime).isEqualTo(original.backupTime);
        assertThat(reparsed.s3Suffix).isEqualTo(original.s3Suffix);
        assertThat(reparsed.snapName).isEqualTo(original.snapName);
        assertThat(reparsed.toString()).isEqualTo(original.toString());
    }

    @Test
    public void roundTripWithoutOptionalParts() throws ParseException
    {
        S3VolumeNameInfo original = new S3VolumeNameInfo("rsc", null, 3, BACKUP_TIME, null, null);
        S3VolumeNameInfo reparsed = new S3VolumeNameInfo(original.toString());

        assertThat(reparsed.rscName).isEqualTo(original.rscName);
        assertThat(reparsed.layerSuffix).isEqualTo(original.layerSuffix);
        assertThat(reparsed.vlmNr).isEqualTo(original.vlmNr);
        assertThat(reparsed.s3Suffix).isEqualTo(original.s3Suffix);
        assertThat(reparsed.snapName).isEqualTo(original.snapName);
        assertThat(reparsed.toString()).isEqualTo(original.toString());
    }
}
