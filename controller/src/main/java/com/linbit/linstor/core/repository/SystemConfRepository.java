package com.linbit.linstor.core.repository;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.InvalidValueException;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.propscon.ReadOnlyProps;

/**
 * Provides access to system-wide props.
 * The controller (ctrl) props are those which only the controller uses.
 * The satellite (stlt) props are those which are transfered to the satellites.
 */
public interface SystemConfRepository
{
    @Nullable
    String setCtrlProp(String key, String value, @Nullable String namespace)
        throws InvalidValueException, DatabaseException, InvalidKeyException;

    @Nullable
    String setStltProp(String key, String value)
        throws InvalidValueException, InvalidKeyException, DatabaseException;

    @Nullable
    String removeCtrlProp(String key, @Nullable String namespace)
        throws InvalidKeyException, DatabaseException;

    @Nullable
    String removeStltProp(String key, @Nullable String namespace)
        throws InvalidKeyException, DatabaseException;

    ReadOnlyProps getCtrlConfForView();

    Props getCtrlConfForChange();

    ReadOnlyProps getStltConfForView();
}
