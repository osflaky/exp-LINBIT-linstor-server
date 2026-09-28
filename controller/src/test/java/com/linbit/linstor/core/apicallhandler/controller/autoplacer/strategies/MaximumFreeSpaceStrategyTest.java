package com.linbit.linstor.core.apicallhandler.controller.autoplacer.strategies;

import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.MinMax;
import com.linbit.linstor.core.apicallhandler.controller.autoplacer.AutoplaceStrategy.RatingAdditionalInfo;
import com.linbit.linstor.core.objects.FreeSpaceTracker;
import com.linbit.linstor.core.objects.StorPool;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class MaximumFreeSpaceStrategyTest
{
    private final MaximumFreeSpaceStrategy strategy = new MaximumFreeSpaceStrategy();

    private StorPool storPoolWithFreeCapacity(Optional<Long> freeCapacity)
    {
        StorPool storPool = Mockito.mock(StorPool.class);
        FreeSpaceTracker freeSpaceTracker = Mockito.mock(FreeSpaceTracker.class);
        Mockito.when(storPool.getFreeSpaceTracker()).thenReturn(freeSpaceTracker);
        Mockito.when(freeSpaceTracker.getFreeCapacityLastUpdated()).thenReturn(freeCapacity);
        return storPool;
    }

    @Test
    public void rateByLastKnownFreeCapacity()
    {
        StorPool small = storPoolWithFreeCapacity(Optional.of(1024L));
        StorPool large = storPoolWithFreeCapacity(Optional.of(10 * 1024 * 1024L));

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(small, large), new RatingAdditionalInfo());

        assertThat(ratings.get(small)).isEqualTo(1024.0);
        assertThat(ratings.get(large)).isEqualTo(10.0 * 1024 * 1024);
    }

    @Test
    public void unknownFreeCapacityRatesZero()
    {
        StorPool unknown = storPoolWithFreeCapacity(Optional.empty());

        Map<StorPool, Double> ratings = strategy.rate(Arrays.asList(unknown), new RatingAdditionalInfo());

        assertThat(ratings.get(unknown)).isEqualTo(0.0);
    }

    @Test
    public void strategyMetaData()
    {
        assertThat(strategy.getName()).isEqualTo(ApiConsts.KEY_AUTOPLACE_STRAT_WEIGHT_MAX_FREESPACE);
        assertThat(strategy.getMinMax()).isEqualTo(MinMax.MAXIMIZE);
        assertThat(strategy.getDefaultWeight()).isEqualTo(1.0);
    }
}
