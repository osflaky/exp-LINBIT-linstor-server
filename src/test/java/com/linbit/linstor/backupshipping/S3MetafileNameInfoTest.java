package com.linbit.linstor.backupshipping;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class S3MetafileNameInfoTest
{
    private static final LocalDateTime BACKUP_TIME = LocalDateTime.of(2024, 3, 15, 12, 34, 56);

    @Test
    public void parseSimpleMetaName() throws ParseException
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc_back_20240315_123456.meta");

        assertThat(info.rscName).isEqualTo("rsc");
        assertThat(info.backupId).isEqualTo("back_20240315_123456");
        assertThat(info.backupTime).isEqualTo(BACKUP_TIME);
        assertThat(info.s3Suffix).isEmpty();
        // without explicit snapshot name the backupId is used as snapName
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
    }

    @Test
    public void parseWithSnapName() throws ParseException
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc_back_20240315_123456^mysnap.meta");

        assertThat(info.rscName).isEqualTo("rsc");
        assertThat(info.backupId).isEqualTo("back_20240315_123456");
        assertThat(info.s3Suffix).isEmpty();
        assertThat(info.snapName).isEqualTo("mysnap");
    }

    @Test
    public void parseWithS3Suffix() throws ParseException
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc_back_20240315_123456:suffix.meta");

        assertThat(info.rscName).isEqualTo("rsc");
        // the s3 suffix keeps its leading colon
        assertThat(info.s3Suffix).isEqualTo(":suffix");
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
    }

    @Test
    public void parseWithS3SuffixAndSnapName() throws ParseException
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc_back_20240315_123456:suffix^mysnap.meta");

        assertThat(info.s3Suffix).isEqualTo(":suffix");
        assertThat(info.snapName).isEqualTo("mysnap");
    }

    @Test
    public void parseDoubledMetaSuffixBecomesS3Suffix() throws ParseException
    {
        // characterization: only the last ".meta" is the file suffix, the rest lands in s3Suffix
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc_back_20240315_123456.meta.meta");
        assertThat(info.s3Suffix).isEqualTo(".meta");
    }

    @Test
    public void parseRscNameMayContainUnderscoresAndDashes() throws ParseException
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("my-rsc_1_back_20240315_123456.meta");
        assertThat(info.rscName).isEqualTo("my-rsc_1");
    }

    @Test
    public void parseInvalidNamesThrowParseException()
    {
        // rscName needs at least 2 characters
        ParseException parseExc = catchThrowableOfType(
            ParseException.class,
            () -> new S3MetafileNameInfo("r_back_20240315_123456.meta")
        );
        assertThat(parseExc)
            .hasMessage("Failed to parse r_back_20240315_123456.meta as S3 backup meta file");
        assertThat(parseExc.getErrorOffset()).isZero();

        // date part needs exactly 8 digits
        assertThatThrownBy(() -> new S3MetafileNameInfo("rsc_back_2024031_123456.meta"))
            .isInstanceOf(ParseException.class);

        // ".meta" suffix is mandatory
        assertThatThrownBy(() -> new S3MetafileNameInfo("rsc_back_20240315_123456"))
            .isInstanceOf(ParseException.class);

        assertThatThrownBy(() -> new S3MetafileNameInfo(""))
            .isInstanceOf(ParseException.class);
    }

    @Test
    public void parseImpossibleDateThrowsDateTimeParseException()
    {
        // characterization: the regex only checks for digits, the date parsing itself
        // throws an unchecked DateTimeParseException
        assertThatThrownBy(() -> new S3MetafileNameInfo("rsc_back_99999999_123456.meta"))
            .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void buildFromPartsWithDefaults()
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc", BACKUP_TIME, null, null);

        assertThat(info.backupId).isEqualTo("back_20240315_123456");
        assertThat(info.backupTime).isEqualTo(BACKUP_TIME);
        assertThat(info.s3Suffix).isEmpty();
        assertThat(info.snapName).isEqualTo("back_20240315_123456");
        assertThat(info.toFullBackupId()).isEqualTo("rsc_back_20240315_123456");
        assertThat(info.toString()).isEqualTo("rsc_back_20240315_123456.meta");
    }

    @Test
    public void buildFromPartsWithSnapNameAndSuffix()
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc", BACKUP_TIME, ":sfx", "mysnap");

        assertThat(info.toFullBackupId()).isEqualTo("rsc_back_20240315_123456:sfx^mysnap");
        assertThat(info.toString()).isEqualTo("rsc_back_20240315_123456:sfx^mysnap.meta");
    }

    @Test
    public void buildFromPartsSnapNameEqualToBackupIdIsOmitted()
    {
        S3MetafileNameInfo info = new S3MetafileNameInfo("rsc", BACKUP_TIME, "", "back_20240315_123456");
        assertThat(info.toString()).isEqualTo("rsc_back_20240315_123456.meta");
    }

    @Test
    public void roundTripKeepsAllFields() throws ParseException
    {
        S3MetafileNameInfo original = new S3MetafileNameInfo("my-rsc", BACKUP_TIME, ":sfx", "mysnap");
        S3MetafileNameInfo reparsed = new S3MetafileNameInfo(original.toString());

        assertThat(reparsed.rscName).isEqualTo(original.rscName);
        assertThat(reparsed.backupId).isEqualTo(original.backupId);
        assertThat(reparsed.backupTime).isEqualTo(original.backupTime);
        assertThat(reparsed.s3Suffix).isEqualTo(original.s3Suffix);
        assertThat(reparsed.snapName).isEqualTo(original.snapName);
        assertThat(reparsed.toString()).isEqualTo(original.toString());
    }

    @Test
    public void roundTripWithoutOptionalParts() throws ParseException
    {
        S3MetafileNameInfo original = new S3MetafileNameInfo("rsc", BACKUP_TIME, null, null);
        S3MetafileNameInfo reparsed = new S3MetafileNameInfo(original.toString());

        assertThat(reparsed.rscName).isEqualTo(original.rscName);
        assertThat(reparsed.backupId).isEqualTo(original.backupId);
        assertThat(reparsed.s3Suffix).isEqualTo(original.s3Suffix);
        assertThat(reparsed.snapName).isEqualTo(original.snapName);
        assertThat(reparsed.toString()).isEqualTo(original.toString());
    }
}
