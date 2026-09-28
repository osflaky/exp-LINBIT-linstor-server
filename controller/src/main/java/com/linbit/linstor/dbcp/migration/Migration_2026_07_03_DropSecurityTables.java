package com.linbit.linstor.dbcp.migration;

import com.linbit.linstor.DatabaseInfo.DbProduct;
import com.linbit.linstor.dbdrivers.SQLUtils;

import java.sql.Connection;

@SuppressWarnings("checkstyle:typename")
@Migration(
    version = "2026.07.03.10.00",
    description = "Drop security tables"
)
public class Migration_2026_07_03_DropSecurityTables extends LinstorMigration
{
    private static final String[] SEC_VIEWS =
    {
        "SEC_IDENTITIES_LOAD",
        "SEC_ROLES_LOAD",
        "SEC_TYPES_LOAD",
        "SEC_TYPE_RULES_LOAD"
    };

    private static final String[] SEC_TABLES =
    {
        // ordered children first to satisfy foreign key constraints
        "SEC_DFLT_ROLES",
        "SEC_ACL_MAP",
        "SEC_ID_ROLE_MAP",
        "SEC_OBJECT_PROTECTION",
        "SEC_TYPE_RULES",
        "SEC_ROLES",
        "SEC_IDENTITIES",
        "SEC_TYPES",
        "SEC_ACCESS_TYPES",
        "SEC_CONFIGURATION"
    };

    @Override
    public void migrate(Connection connection, DbProduct dbProduct) throws Exception
    {
        for (String view : SEC_VIEWS)
        {
            if (MigrationUtils.tableExists(connection, view))
            {
                SQLUtils.runSql(connection, MigrationUtils.dropView(dbProduct, view));
            }
        }
        for (String table : SEC_TABLES)
        {
            if (MigrationUtils.tableExists(connection, table))
            {
                SQLUtils.runSql(connection, MigrationUtils.dropTable(dbProduct, table));
            }
        }
    }
}
