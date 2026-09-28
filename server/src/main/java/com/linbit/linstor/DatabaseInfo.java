package com.linbit.linstor;

import com.linbit.ImplementationError;


public class DatabaseInfo
{
    public enum DbProduct
    {
        UNKNOWN,
        H2,
        DERBY,
        DB2,
        DB2_I,
        DB2_Z,
        POSTGRESQL,
        ORACLE_RDBMS,
        MSFT_SQLSERVER,
        MYSQL,
        MARIADB,
        INFORMIX,
        ASE;

        public String displayName()
        {
            return switch (this)
            {
                case UNKNOWN -> "unknown";
                case H2 -> "H2";
                case DERBY -> "Apache Derby";
                case DB2 -> "DB2/LUW";
                case DB2_I -> "DB2/System i";
                case DB2_Z -> "DB2/System z";
                case POSTGRESQL -> "PostgreSQL";
                case ORACLE_RDBMS -> "Oracle RDBMS";
                case MSFT_SQLSERVER -> "Microsoft SQLServer";
                case MYSQL -> "Oracle MySQL";
                case MARIADB -> "MariaDB";
                case INFORMIX -> "IBM Informix";
                case ASE -> "SAP Adaptive Server Enterprise (Sybase)";
                default -> throw new ImplementationError(
                    "Missing case statement for enum " + name() + " in class " +
                    getClass().getCanonicalName()
                );
            };
        }

        public String dbType()
        {
            return switch (this)
            {
                case UNKNOWN -> "unknown";
                case H2 -> "h2";
                case DERBY -> "derby";
                case DB2 -> "db2";
                case DB2_I -> "db2/i";
                case DB2_Z -> "db2/z";
                case POSTGRESQL -> "postgresql";
                case ORACLE_RDBMS -> "oracle";
                case MSFT_SQLSERVER -> "mssql";
                case MYSQL -> "mysql";
                case MARIADB -> "mariadb";
                case INFORMIX -> "informix";
                case ASE -> "sybase";
                default -> throw new ImplementationError(
                    "Missing case statement for enum " + name() + " in class " +
                        getClass().getCanonicalName()
                );
            };
        }
    }

    public static final int[] H2_MIN_VERSION = {1, 2};
    public static final int[] DERBY_MIN_VERSION = {10, 11};
    public static final int[] DB2_MIN_VERSION = {9, 7};
    public static final int[] POSTGRES_MIN_VERSION = {9, 0};
    public static final int[] MYSQL_MIN_VERSION = {5, 1};
    public static final int[] MARIADB_MIN_VERSION = {5, 1};
    public static final int[] INFORMIX_MIN_VERSION = {12, 10};

    private static final String ID_H2       = "H2";
    private static final String ID_DB2      = "DB2";
    private static final String ID_PGSQL    = "POSTGRESQL";
    private static final String ID_MYSQL    = "MYSQL";

    private static final String SUB_ID_MARIADB = "MARIADB";

    public static DbProduct getDbProduct(String databaseProductName, String databaseProductVersion)
    {
        DbProduct dbProd = DbProduct.UNKNOWN;
        String dbUpperName = databaseProductName.toUpperCase();
        if (dbUpperName.equals(ID_H2))
        {
            dbProd = DbProduct.H2;
        }
        else
        if (dbUpperName.equals(ID_DB2))
        {
            dbProd = DbProduct.DB2;
        }
        else
        if (dbUpperName.startsWith(ID_DB2))
        {
            int splitIdx = dbUpperName.indexOf('/');
            if (splitIdx != -1)
            {
                String db2Id = dbUpperName.substring(0, splitIdx);
                if (db2Id.equals(ID_DB2))
                {
                    dbProd = DbProduct.DB2;
                }
            }
        }
        else
        if (dbUpperName.equals(ID_PGSQL))
        {
            dbProd = DbProduct.POSTGRESQL;
        }
        else
        if (dbUpperName.equals(ID_MYSQL))
        {
            String dbVsn = databaseProductVersion;
            dbVsn = dbVsn.toUpperCase();
            if (dbVsn.contains(SUB_ID_MARIADB))
            {
                dbProd = DbProduct.MARIADB;
            }
            else
            {
                dbProd = DbProduct.MYSQL;
            }
        }
        else
        if (dbUpperName.equals(SUB_ID_MARIADB))
        {
            dbProd = DbProduct.MARIADB;
        }
        return dbProd;
    }

    private DatabaseInfo()
    {
    }
}
