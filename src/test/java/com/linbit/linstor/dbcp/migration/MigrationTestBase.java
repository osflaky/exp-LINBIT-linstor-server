package com.linbit.linstor.dbcp.migration;

import com.linbit.linstor.dbcp.DbMigrater;
import com.linbit.linstor.dbdrivers.DatabaseDriverInfo;
import com.linbit.linstor.testutils.EmptyErrorReporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;

/**
 * Base class for tests of single SQL database migrations.
 *
 * The usual pattern is:
 * <ol>
 * <li>{@link #startDbAtVersion(String, String)} with the version just before the migration under test</li>
 * <li>insert legacy data via plain SQL matching the pre-migration schema</li>
 * <li>{@link #migrateTo(String)} with the version of the migration under test</li>
 * <li>assert the transformed schema and data, and that unrelated rows are untouched</li>
 * </ol>
 */
public abstract class MigrationTestBase
{
    protected final DbMigrater migrater = new DbMigrater(new EmptyErrorReporter());
    protected final DatabaseDriverInfo dbInfo = DatabaseDriverInfo.createDriverInfo("h2");
    protected Connection conn;

    /**
     * Opens a fresh in-memory H2 database (unique name per test!) and migrates it up to the given version.
     */
    protected void startDbAtVersion(String dbName, String version) throws Exception
    {
        conn = DriverManager.getConnection("jdbc:h2:mem:" + dbName);
        migrater.setSchema(conn, dbInfo);
        migrater.migrateToVersion(conn, dbInfo, version);
    }

    protected void migrateTo(String version) throws Exception
    {
        migrater.migrateToVersion(conn, dbInfo, version);
    }

    @After
    public void closeConnection() throws SQLException
    {
        if (conn != null)
        {
            conn.close();
            conn = null;
        }
    }

    protected void executeUpdate(String sql) throws SQLException
    {
        try (Statement stmt = conn.createStatement())
        {
            stmt.executeUpdate(sql);
        }
        conn.commit();
    }

    /**
     * Runs the given query and returns the first column of the single result row as string.
     * Fails if the query does not return exactly one row.
     */
    protected String querySingleString(String sql) throws SQLException
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        )
        {
            if (!rs.next())
            {
                throw new AssertionError("Expected exactly one row, but got none. Query: " + sql);
            }
            String value = rs.getString(1);
            if (rs.next())
            {
                throw new AssertionError("Expected exactly one row, but got more. Query: " + sql);
            }
            return value;
        }
    }

    /**
     * Runs the given query and returns the first column of the single result row as Integer (null if SQL NULL).
     * Fails if the query does not return exactly one row.
     */
    protected Integer querySingleInt(String sql) throws SQLException
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        )
        {
            if (!rs.next())
            {
                throw new AssertionError("Expected exactly one row, but got none. Query: " + sql);
            }
            int value = rs.getInt(1);
            Integer result = rs.wasNull() ? null : value;
            if (rs.next())
            {
                throw new AssertionError("Expected exactly one row, but got more. Query: " + sql);
            }
            return result;
        }
    }

    protected int countRows(String fromAndWhere) throws SQLException
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + fromAndWhere)
        )
        {
            rs.next();
            return rs.getInt(1);
        }
    }
}
