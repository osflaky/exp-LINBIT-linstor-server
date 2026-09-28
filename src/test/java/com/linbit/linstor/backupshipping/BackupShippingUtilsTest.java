package com.linbit.linstor.backupshipping;

import java.time.LocalDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BackupShippingUtilsTest
{
    @Test
    public void defaultEmptyReturnsEmptyStringForNull()
    {
        assertThat(BackupShippingUtils.defaultEmpty(null)).isEmpty();
    }

    @Test
    public void defaultEmptyKeepsNonNullValues()
    {
        assertThat(BackupShippingUtils.defaultEmpty("")).isEmpty();
        assertThat(BackupShippingUtils.defaultEmpty("value")).isEqualTo("value");
        assertThat(BackupShippingUtils.defaultEmpty(" ")).isEqualTo(" ");
    }

    @Test
    public void generateBackupNameFormatsTimestamp()
    {
        assertThat(BackupShippingUtils.generateBackupName(LocalDateTime.of(2024, 3, 15, 12, 34, 56)))
            .isEqualTo("back_20240315_123456");
    }

    @Test
    public void generateBackupNameZeroPadsFields()
    {
        assertThat(BackupShippingUtils.generateBackupName(LocalDateTime.of(2024, 1, 2, 3, 4, 5)))
            .isEqualTo("back_20240102_030405");
    }

    @Test
    public void propsNamespaceConstants()
    {
        assertThat(BackupShippingUtils.BACKUP_TARGET_PROPS_NAMESPC).isEqualTo("BackupShipping/Target");
        assertThat(BackupShippingUtils.BACKUP_SOURCE_PROPS_NAMESPC).isEqualTo("BackupShipping/Source");
    }
}
