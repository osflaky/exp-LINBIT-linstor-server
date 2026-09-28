package com.linbit.linstor.dbdrivers.interfaces;

import com.linbit.linstor.dbdrivers.DatabaseException;

public interface GenericDatabaseDriver<DATA>
{
    /**
     * Persists the given DATA object into the database.
     *
     */
    void create(DATA data) throws DatabaseException;

    /**
     * Updates or creates the given DATA object.
     *
     */
    void upsert(DATA dataRef) throws DatabaseException;

    /**
     * Removes the given DATA from the database
     *
     *
     */
    void delete(DATA data) throws DatabaseException;

    /**
     * Removes all data from the current database table
     *
     */
    void truncate() throws DatabaseException;
}
