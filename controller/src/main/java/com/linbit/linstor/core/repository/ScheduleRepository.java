package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.identifier.ScheduleName;
import com.linbit.linstor.core.objects.Schedule;

public interface ScheduleRepository
{
    @Nullable
    Schedule get(ScheduleName scheduleName);

    void put(Schedule schedule);

    void remove(ScheduleName scheduleName);

    CoreModule.ScheduleMap getMapForView();
}
