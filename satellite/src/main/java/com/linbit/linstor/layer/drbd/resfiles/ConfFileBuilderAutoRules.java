package com.linbit.linstor.layer.drbd.resfiles;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.propscon.ReadOnlyProps;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdRscData;
import com.linbit.linstor.storage.data.adapter.drbd.DrbdVlmData;

import java.util.HashMap;

public class ConfFileBuilderAutoRules
{
    private final HashMap<String, AutoRule> autoRules = new HashMap<>();

    public ConfFileBuilderAutoRules(DrbdVlmData<Resource> drbdVlmData)
    {
        appendRules();
        appendRules(drbdVlmData);
    }

    public ConfFileBuilderAutoRules(DrbdRscData<Resource> drbdRscData)
    {
        appendRules();
    }

    private void appendRules()
    {
    }

    private void appendRules(DrbdVlmData<Resource> drbdVlmDataRef)
    {
        autoRules.put(
            ApiConsts.NAMESPC_DRBD_DISK_OPTIONS + "/rs-discard-granularity",
            new AutoRule(
                ApiConsts.NAMESPC_DRBD_OPTIONS + "/" + ApiConsts.KEY_DRBD_AUTO_RS_DISCARD_GRANULARITY,
                drbdVlmDataRef.getVolume().getVolumeDefinition().getProps(),
                false
            )
        );
        autoRules.put(
            ApiConsts.NAMESPC_DRBD_DISK_OPTIONS + "/discard-granularity",
            new AutoRule(
                ApiConsts.NAMESPC_LINSTOR_DRBD + "/" + ApiConsts.KEY_DRBD_AUTO_DISCARD_GRANULARITY,
                drbdVlmDataRef.getVolume().getVolumeDefinition().getProps(),
                false
            )
        );
    }

    public @Nullable AutoRule get(String keyWithNamespaceRef)
    {
        return autoRules.get(keyWithNamespaceRef);
    }

    public static class AutoRule
    {
        public final String key;
        public final ReadOnlyProps props;
        public final boolean isNullAllowed;

        private AutoRule(String keyRef, ReadOnlyProps propsRef, boolean isNullAllowedRef)
        {
            key = keyRef;
            props = propsRef;
            isNullAllowed = isNullAllowedRef;
        }
    }
}
