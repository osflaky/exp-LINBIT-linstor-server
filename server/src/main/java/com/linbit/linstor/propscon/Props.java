package com.linbit.linstor.propscon;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.transaction.TransactionObject;

/**
 * Common interface for Containers that hold linstor property maps
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface Props extends TransactionObject, ReadOnlyProps
{
    @Nullable
    String setProp(String key, String value)
        throws InvalidKeyException, InvalidValueException, DatabaseException;

    @Nullable
    String setProp(String key, String value, @Nullable String namespace)
        throws InvalidKeyException, InvalidValueException, DatabaseException;

    @Nullable
    String removeProp(String key)
        throws InvalidKeyException, DatabaseException;

    @Nullable
    String removeProp(String key, @Nullable String namespace)
        throws InvalidKeyException, DatabaseException;
    boolean removeNamespace(String namespaceRef)
        throws DatabaseException;

    @Override
    @Nullable Props getNamespace(@Nullable String namespace);

    void loadAll() throws DatabaseException;

    void clear() throws DatabaseException;

    void delete() throws DatabaseException;
}
