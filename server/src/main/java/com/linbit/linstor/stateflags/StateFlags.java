package com.linbit.linstor.stateflags;

import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.transaction.TransactionObject;

/**
 * Manages a set of state flags with access control and persistence.
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface StateFlags<T extends Flags> extends TransactionObject
{
    void enableAllFlags()
        throws DatabaseException;

    void disableAllFlags()
        throws DatabaseException;

    void enableFlags(T... flags)
        throws DatabaseException;

    void disableFlags(T... flags)
        throws DatabaseException;

    void enableFlagsExcept(T... flags)
        throws DatabaseException;

    void resetFlagsTo(T... flags)
        throws DatabaseException;

    void disableFlagsExcept(T... flags)
        throws DatabaseException;

    boolean isSet(T... flags);

    boolean isUnset(T... flags);

    boolean isSomeSet(T... flags);

    boolean isSomeUnset(T... flags);

    long getFlagsBits();

    long getValidFlagsBits();

}
