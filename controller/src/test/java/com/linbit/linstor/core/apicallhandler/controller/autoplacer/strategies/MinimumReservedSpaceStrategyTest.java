package com.linbit.linstor.core.apicallhandler.controller.autoplacer.strategies;

import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.MinMax;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.RatingAdditionalInfo;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.Snapshot;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class MinimumReservedSpaceStrategyTest
{
    private final MinimumReservedSpaceStrategy strategy = new MinimumReservedSpaceStrategy();

    @SuppressWarnings("unchecked")
    private static VlmProviderObject<Resource> rscVlmObjWithUsableSize(long usableSize)
    {
        VlmProviderObject<Resource> vlmObj = Mockito.mock(VlmProviderObject.class);
        Mockito.when(vlmObj.getUsableSize()).thenReturn(usableSize);
        return vlmObj;
    }

    @SuppressWarnings("unchecked")
    private static VlmProviderObject<Snapshot> snapVlmObjWithUsableSize(long usableSize)
    {
        VlmProviderObject<Snapshot> vlmObj = Mockito.mock(VlmProviderObject.class);
        Mockito.when(vlmObj.getUsableSize()).thenReturn(usableSize);
        return vlmObj;
    }

    @Test
    public void sumsUsableSizesOfVolumesAndSnapshots()
    {
        StorPool storPool = Mockito.mock(StorPool.class);
        Mockito.when(storPool.getDeviceProviderKind()).thenReturn(DeviceProviderKind.LVM);
        List<VlmProviderObject<Resource>> vlmObjs = Arrays.asList(
            rscVlmObjWithUsableSize(1024L),
            rscVlmObjWithUsableSize(2048L)
        );
        List<VlmProviderObject<Snapshot>> snapVlmObjs = Arrays.asList(snapVlmObjWithUsableSize(512L));
        Mockito.when(storPool.getVolumes()).thenReturn(vlmObjs);
        Mockito.when(storPool.getSnapVolumes()).thenReturn(snapVlmObjs);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(1024.0 + 2048.0 + 512.0);
    }

    @Test
    public void unknownUsableSizeIsIgnored()
    {
        StorPool storPool = Mockito.mock(StorPool.class);
        Mockito.when(storPool.getDeviceProviderKind()).thenReturn(DeviceProviderKind.LVM_THIN);
        List<VlmProviderObject<Resource>> vlmObjs = Arrays.asList(
            rscVlmObjWithUsableSize(-1L),
            rscVlmObjWithUsableSize(4096L)
        );
        Mockito.when(storPool.getVolumes()).thenReturn(vlmObjs);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(4096.0);
    }

    @Test
    public void storPoolWithoutBackingDeviceRatesZero()
    {
        StorPool storPool = Mockito.mock(StorPool.class);
        Mockito.when(storPool.getDeviceProviderKind()).thenReturn(DeviceProviderKind.DISKLESS);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(0.0);
        Mockito.verify(storPool, Mockito.never()).getVolumes();
    }

    @Test
    public void strategyMetaData()
    {
        assertThat(strategy.getName()).isEqualTo(ApiConsts.KEY_AUTOPLACE_STRAT_WEIGHT_MIN_RESERVED_SPACE);
        assertThat(strategy.getMinMax()).isEqualTo(MinMax.MINIMIZE);
        // no override -> interface default
        assertThat(strategy.getDefaultWeight()).isEqualTo(0.0);
    }
}
