package com.linbit.linstor.dbdrivers.sql;

import com.linbit.ImplementationError;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.dbdrivers.DatabaseTable;
import com.linbit.linstor.dbdrivers.DatabaseTable.Column;
import com.linbit.linstor.dbdrivers.DbEngine.DataToString;
import com.linbit.linstor.dbdrivers.interfaces.updater.SingleColumnDatabaseDriver;
import com.linbit.linstor.logging.ErrorReporter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.function.Function;

class SQLSingleColumnDriver<DATA, INPUT_TYPE, DB_TYPE> implements SingleColumnDatabaseDriver<DATA, INPUT_TYPE>
{
    private final SQLEngine sqlEngine;
    private final ErrorReporter errorReporter;
    private final Map<Column, Function<DATA, Object>> setters;
    private final Column colToUpdate;
    private final DataToString<DATA> dataToString;
    private final Function<DATA, String> dataValueToString;

    private final DatabaseTable table;
    private final String updateStatement;

    private final DataToString<INPUT_TYPE> inputToStringFct;

    SQLSingleColumnDriver(
        SQLEngine sqlEngineRef,
        ErrorReporter errorReporterRef,
        Map<Column, Function<DATA, Object>> settersRef,
        Column colToUpdateRef,
        Function<INPUT_TYPE, DB_TYPE> mapperRef,
        DataToString<DATA> dataToStringRef,
        Function<DATA, String> dataValueToStringRef,
        DataToString<INPUT_TYPE> inputToStringRef
    )
    {
        sqlEngine = sqlEngineRef;
        errorReporter = errorReporterRef;
        setters = settersRef;
        colToUpdate = colToUpdateRef;
        dataToString = dataToStringRef;
        dataValueToString = dataValueToStringRef;
        inputToStringFct = inputToStringRef;

        table = colToUpdateRef.getTable();
        updateStatement = sqlEngine.generateUpdateStatement(colToUpdateRef);
    }

    @Override
    public void update(DATA parentRef, INPUT_TYPE oldElementRef) throws DatabaseException
    {
        try
        {
            if (oldElementRef instanceof byte[])
            {
                errorReporter.logTrace(
                    "Updating %s's %s from [%s] to [%s] %s",
                    table.getName(),
                    colToUpdate.getName(),
                    "<Array of bytes>",
                    dataValueToString.apply(parentRef) == null ? "null" : "<Array of bytes>",
                    dataToString.toString(parentRef)
                );
            }
            else
            {
                errorReporter.logTrace(
                    "Updating %s's %s from [%s] to [%s] %s",
                    table.getName(),
                    colToUpdate.getName(),
                    oldElementRef == null ? "null" : inputToStringFct.toString(oldElementRef),
                    dataValueToString.apply(parentRef),
                    dataToString.toString(parentRef)
                );
            }
            try (PreparedStatement stmt = sqlEngine.getConnection().prepareStatement(updateStatement))
            {
                int idx = fillSetter(stmt, 1, (DB_TYPE) setters.get(colToUpdate).apply(parentRef));
                sqlEngine.setPrimaryValues(setters, stmt, idx, table, parentRef);

                stmt.executeUpdate();
            }
            if (oldElementRef instanceof byte[])
            {
                errorReporter.logTrace(
                    "%s's %s updated from [%s] to [%s] %s",
                    table.getName(),
                    colToUpdate.getName(),
                    "<Array of bytes>",
                    dataValueToString.apply(parentRef) == null ? "null" : "<Array of bytes>",
                    dataToString.toString(parentRef)
                );
            }
            else
            {
                errorReporter.logTrace(
                    "%s's %s updated from [%s] to [%s] %s",
                    table.getName(),
                    colToUpdate.getName(),
                    oldElementRef == null ? "null" : inputToStringFct.toString(oldElementRef),
                    dataValueToString.apply(parentRef),
                    dataToString.toString(parentRef)
                );
            }
        }
        catch (SQLException sqlExc)
        {
            throw new DatabaseException(sqlExc);
        }
    }

    /**
     * This method performs the necessary stmt.set* method-calls
     *
     * @return the index of the next column which was not yet set.
     */
    protected int fillSetter(PreparedStatement stmt, int startIdx, DB_TYPE element)
        throws SQLException
    {
        if (colToUpdate.getSqlType() == Types.BLOB)
        {
            if (element == null)
            {
                stmt.setNull(startIdx, colToUpdate.getSqlType());
            }
            else if (element instanceof byte[] arr)
            {
                // BLOB needs special treatment as i.e. PSQL does not support setting byte[] as a blob
                stmt.setBytes(startIdx, arr);
            }
            else
            {
                throw new ImplementationError("Invalid class (" + element.getClass() + ") for BLOB type");
            }
        }
        else
        {
            stmt.setObject(startIdx, element, colToUpdate.getSqlType());
        }
        return startIdx + 1;
    }
}
