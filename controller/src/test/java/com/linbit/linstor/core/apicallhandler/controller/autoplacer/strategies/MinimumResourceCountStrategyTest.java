package com.linbit.linstor.core.apicallhandler.controller.autoplacer.strategies;

import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.MinMax;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.RatingAdditionalInfo;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.StorPool;
import com.linbit.linstor.storage.interfaces.categories.resource.AbsRscLayerObject;
import com.linbit.linstor.storage.interfaces.categories.resource.VlmProviderObject;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class MinimumResourceCountStrategyTest
{
    private final MinimumResourceCountStrategy strategy = new MinimumResourceCountStrategy();

    @SuppressWarnings("unchecked")
    private VlmProviderObject<Resource> vlmObjOf(Resource rsc)
    {
        VlmProviderObject<Resource> vlmObj = Mockito.mock(VlmProviderObject.class);
        AbsRscLayerObject<Resource> rscLayerObj = Mockito.mock(AbsRscLayerObject.class);
        Mockito.when(vlmObj.getRscLayerObject()).thenReturn(rscLayerObj);
        Mockito.when(rscLayerObj.getAbsResource()).thenReturn(rsc);
        return vlmObj;
    }

    @SafeVarargs
    private StorPool storPoolWith(VlmProviderObject<Resource>... vlmObjs)
    {
        StorPool storPool = Mockito.mock(StorPool.class);
        Mockito.when(storPool.getVolumes()).thenReturn(Arrays.asList(vlmObjs));
        return storPool;
    }

    @Test
    public void countsDistinctResources()
    {
        Resource rscA = Mockito.mock(Resource.class);
        Resource rscB = Mockito.mock(Resource.class);

        StorPool storPool = storPoolWith(vlmObjOf(rscA), vlmObjOf(rscB));

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(2.0);
    }

    @Test
    public void multipleVlmObjectsOfSameResourceCountOnce()
    {
        // e.g. a pmem storage pool can hold vlmProviderObjects of the cache
        // as well as the writecache layer of the same resource
        Resource rsc = Mockito.mock(Resource.class);

        StorPool storPool = storPoolWith(vlmObjOf(rsc), vlmObjOf(rsc));

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(1.0);
    }

    @Test
    public void emptyStorPoolRatesZero()
    {
        StorPool storPool = storPoolWith();

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(storPool), new RatingAdditionalInfo());

        assertThat(ratings.get(storPool)).isEqualTo(0.0);
    }

    @Test
    public void strategyMetaData()
    {
        assertThat(strategy.getName()).isEqualTo(ApiConsts.KEY_AUTOPLACE_STRAT_WEIGHT_MIN_RSC_COUNT);
        assertThat(strategy.getMinMax()).isEqualTo(MinMax.MINIMIZE);
        assertThat(strategy.getDefaultWeight()).isEqualTo(0.00001);
    }
}
