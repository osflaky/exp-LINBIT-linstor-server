package com.linbit.linstor.dbdrivers;

import com.linbit.ImplementationError;

import static com.linbit.linstor.dbdrivers.derby.DbConstants.DATABASE_SCHEMA_NAME;

public interface DatabaseDriverInfo
{
    /*
     * DO NOT remove the escaped " in the SQL statement since some database dialects automatically toUpper cases names
     * in SQL statements and other dialects don't. This is the way FlyWay set up the table, and we have to keep using it
     * with the given lower- or upper-case.
     */
    String CREATE_TBL_SCHEMA_HISTORY = """
        CREATE TABLE IF NOT EXISTS "FLYWAY_SCHEMA_HISTORY"(
            "installed_rank" INT NOT NULL PRIMARY KEY,
            "version" VARCHAR(50),
            "description" VARCHAR(200) NOT NULL,
            "type" VARCHAR(20) NOT NULL,
            "script" VARCHAR(1000) NOT NULL,
            "checksum" INT,
            "installed_by" VARCHAR(100) NOT NULL,
            "installed_on" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
            "execution_time" INT NOT NULL,
            "success" BOOLEAN NOT NULL)""";
    String DB_VERSIONS_QUERY_STMT = "SELECT \"installed_rank\", \"version\" FROM \"FLYWAY_SCHEMA_HISTORY\" " +
        "WHERE \"version\" IS NOT NULL ORDER BY \"version\"";
    String DB_VERSION_HIGHEST_RANK = "SELECT \"installed_rank\" FROM \"FLYWAY_SCHEMA_HISTORY\" " +
        "WHERE \"version\" IS NOT NULL ORDER BY \"version\" DESC LIMIT 1";
    String DB_VERSION_INSERT = "INSERT INTO \"FLYWAY_SCHEMA_HISTORY\" " +
        "VALUES( ?, ?, ?, 'JDBC', ?, null, 'LINSTOR', CURRENT_TIMESTAMP, ?, TRUE)";

    enum DatabaseType
    {
        SQL,
        K8S_CRD;

        public String displayName()
        {
            return switch (this)
            {
                case SQL -> "SQL";
                case K8S_CRD -> "Kubernetes-CRD";
                default -> throw new ImplementationError(
                    "Missing case statement for enum " + name() + " in class " +
                    getClass().getCanonicalName()
                );
            };
        }
    }

    static DatabaseDriverInfo createDriverInfo(final String dbType)
    {
        return switch (dbType)
        {
            case "h2" -> new H2DatabaseInfo();
            case "derby" -> new DerbyDatabaseInfo();
            case "db2" -> new Db2DatabaseInfo();
            case "postgresql" -> new PostgresqlDatabaseInfo();
            case "mysql", "mariadb" -> new MariaDBInfo(dbType);
            default -> throw new RuntimeException(String.format("Database type '%s' not implemented.", dbType));
        };
    }

    String jdbcUrl(String dbPath);
    String jdbcInMemoryUrl();

    default String createSchemaStatement()
    {
        return String.format("CREATE SCHEMA IF NOT EXISTS \"%s\";", DATABASE_SCHEMA_NAME);
    }

    default String createVersionTableStatement()
    {
        return CREATE_TBL_SCHEMA_HISTORY;
    }

    default String queryVersionsStatement()
    {
        return DB_VERSIONS_QUERY_STMT;
    }

    default String getDbVersionHighestRankStmt()
    {
        return DB_VERSION_HIGHEST_RANK;
    }

    default String versionTableInsertStatement()
    {
        return DB_VERSION_INSERT;
    }

    String prepareInit(String initSQL);
}
