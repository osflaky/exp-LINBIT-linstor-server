package com.linbit.linstor.storage.interfaces.categories.resource;

import com.linbit.GenericName;
import com.linbit.linstor.api.interfaces.RscDfnLayerDataApi;
import com.linbit.linstor.core.identifier.ResourceName;
import com.linbit.linstor.core.identifier.SnapshotName;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.storage.interfaces.categories.LayerObject;
import com.linbit.utils.StringUtils;

/**
 * Represents a resource definition layer object in the storage layer stack.
 *
 * @author Gabor Hernadi &lt;gabor.hernadi@linbit.com&gt;
 */
public interface RscDfnLayerObject extends LayerObject, Comparable<RscDfnLayerObject>
{
    String getRscNameSuffix();

    ResourceName getResourceName();

    SnapshotName getSnapshotName();

    void delete() throws DatabaseException;

    RscDfnLayerDataApi getApiData();

    @Override
    default int compareTo(RscDfnLayerObject other)
    {
        int cmp = getResourceName().compareTo(other.getResourceName());
        if (cmp == 0)
        {
            cmp = StringUtils.compareToNullable(getRscNameSuffix(), other.getRscNameSuffix());
            if (cmp == 0)
            {
                cmp = GenericName.compareToNullable(getSnapshotName(), other.getSnapshotName());
            }
        }
        return cmp;
    }
}
