package com.linbit.linstor.core.apicallhandler.controller.autoplacer.strategies;

import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.MinMax;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.RatingAdditionalInfo;
import com.linbit.linstor.core.apicallhandler.response.ApiException;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.core.objects.StorPoolDefinition;
import com.linbit.linstor.core.objects.Volume;
import com.linbit.linstor.core.repository.SystemConfRepository;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.propscon.ReadOnlyProps;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MaximumThroughputRemainingStrategyTest
{
    private SystemConfRepository sysCfgRepo;
    private MaximumThroughputRemainingStrategy strategy;

    private StorPool storPool;
    private Props storPoolProps;

    @Before
    public void setUp() throws Exception
    {
        sysCfgRepo = Mockito.mock(SystemConfRepository.class);
        Mockito.when(sysCfgRepo.getCtrlConfForView()).thenReturn(Mockito.mock(ReadOnlyProps.class));
        strategy = new MaximumThroughputRemainingStrategy(sysCfgRepo);

        storPool = Mockito.mock(StorPool.class);
        storPoolProps = Mockito.mock(Props.class);
        StorPoolDefinition storPoolDfn = Mockito.mock(StorPoolDefinition.class);
        Mockito.when(storPool.getProps()).thenReturn(storPoolProps);
        Mockito.when(storPool.getDefinition()).thenReturn(storPoolDfn);
        Mockito.when(storPoolDfn.getProps()).thenReturn(Mockito.mock(Props.class));
        Mockito.when(storPool.getVolumes()).thenReturn(new ArrayList<>());
    }

    private void setMaxThroughput(String value) throws Exception
    {
        Mockito.when(
            storPoolProps.getProp(ApiConsts.KEY_AUTOPLACE_MAX_THROUGHPUT, ApiConsts.NAMESPC_AUTOPLACER)
        ).thenReturn(value);
    }

    private Volume volumeWithThrottle(String read, String write) throws Exception
    {
        Volume vlm = Mockito.mock(Volume.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(vlm.getProps().getProp(ApiConsts.KEY_SYS_FS_BLKIO_THROTTLE_READ, ApiConsts.NAMESPC_SYS_FS))
            .thenReturn(read);
        Mockito.when(vlm.getProps().getProp(ApiConsts.KEY_SYS_FS_BLKIO_THROTTLE_WRITE, ApiConsts.NAMESPC_SYS_FS))
            .thenReturn(write);
        return vlm;
    }

    @SuppressWarnings("unchecked")
    private static VlmProviderObject<Resource> vlmObjOf(Volume vlm)
    {
        VlmProviderObject<Resource> vlmObj = Mockito.mock(VlmProviderObject.class);
        Mockito.doReturn(vlm).when(vlmObj).getVolume();
        return vlmObj;
    }

    @Test
    public void withoutConfigurationEverythingRatesZero()
    {
        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(0.0);
    }

    @Test
    public void volumeThrottleReducesRemainingThroughput() throws Exception
    {
        setMaxThroughput("1000.0");
        Volume vlm = volumeWithThrottle("300.0", "200.0");
        List<VlmProviderObject<Resource>> vlmObjs = Arrays.asList(vlmObjOf(vlm));
        Mockito.when(storPool.getVolumes()).thenReturn(vlmObjs);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(500.0);
    }

    @Test
    public void sameVolumeBehindMultipleVlmObjectsCountsOnce() throws Exception
    {
        setMaxThroughput("1000.0");
        Volume vlm = volumeWithThrottle("100.0", "50.0");
        List<VlmProviderObject<Resource>> vlmObjs = Arrays.asList(vlmObjOf(vlm), vlmObjOf(vlm));
        Mockito.when(storPool.getVolumes()).thenReturn(vlmObjs);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(850.0);
    }

    @Test
    public void overcommittedStorPoolRatesNegative() throws Exception
    {
        setMaxThroughput("100.0");
        Volume vlm = volumeWithThrottle("300.0", "200.0");
        List<VlmProviderObject<Resource>> vlmObjs = Arrays.asList(vlmObjOf(vlm));
        Mockito.when(storPool.getVolumes()).thenReturn(vlmObjs);

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(-400.0);
    }

    @Test
    public void unparsableThroughputThrowsApiException() throws Exception
    {
        setMaxThroughput("not-a-number");

        assertThatThrownBy(() -> strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo()))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("not-a-number");
    }

    @Test
    public void strategyMetaData()
    {
        assertThat(strategy.getName()).isEqualTo(ApiConsts.KEY_AUTOPLACE_MAX_THROUGHPUT);
        assertThat(strategy.getMinMax()).isEqualTo(MinMax.MAXIMIZE);
        // no override -> interface default
        assertThat(strategy.getDefaultWeight()).isEqualTo(0.0);
    }
}
