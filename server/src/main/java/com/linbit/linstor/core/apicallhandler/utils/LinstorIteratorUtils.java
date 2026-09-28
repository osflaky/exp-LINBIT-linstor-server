package com.linbit.linstor.core.apicallhandler.utils;

import com.linbit.linstor.core.CoreModule.ResourceDefinitionMap;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.ResourceDefinition;
import com.linbit.linstor.core.objects.ResourceGroup;
import com.linbit.linstor.core.objects.VolumeGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

public class LinstorIteratorUtils
{
    private LinstorIteratorUtils()
    {
        // utility class
    }

    public static <T> ArrayList<T> foreachRsc(
        Node nodeRef,
        Function<Resource, T> fct
    )
    {
        ArrayList<T> ret = new ArrayList<>();
        Iterator<Resource> rscIt = nodeRef.iterateResources();
        while (rscIt.hasNext())
        {
            ret.add(fct.apply(rscIt.next()));
        }
        return ret;
    }

    public static <T> Collection<T> foreachRscDfn(
        VolumeGroup vlmGrpRef,
        Function<ResourceDefinition, T> fct
    )
    {
        return foreachRscDfn(vlmGrpRef.getResourceGroup(), fct);
    }

    public static <T> ArrayList<T> foreachRscDfn(
        ResourceGroup rscGrpRef,
        Function<ResourceDefinition, T> fct
    )
    {
        ArrayList<T> ret = new ArrayList<>();
        for (ResourceDefinition rscDfn : getRscDfns(rscGrpRef))
        {
            ret.add(fct.apply(rscDfn));
        }
        return ret;
    }

    public static <T> ArrayList<T> foreachRscDfn(
        ResourceDefinitionMap rscDfnMapRef,
        Function<ResourceDefinition, T> fct
    )
    {
        ArrayList<T> ret = new ArrayList<>();
        for (ResourceDefinition rscDfn : rscDfnMapRef.values())
        {
            ret.add(fct.apply(rscDfn));
        }
        return ret;
    }

    public static Collection<ResourceDefinition> getRscDfns(ResourceGroup rscGrpRef)
    {
        return rscGrpRef.getRscDfns();
    }

    public static Collection<ResourceDefinition> getRscDfns(VolumeGroup vlmGrpRef)
    {
        return getRscDfns(vlmGrpRef.getResourceGroup());
    }

}
