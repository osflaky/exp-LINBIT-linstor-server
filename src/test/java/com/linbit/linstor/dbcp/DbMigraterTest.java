package com.linbit.linstor.dbcp;

import com.linbit.linstor.InitializationException;
import com.linbit.linstor.dbdrivers.DatabaseDriverInfo;
import com.linbit.linstor.testutils.EmptyErrorReporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DbMigraterTest
{
    private static final String FUTURE_MIGRATION_VERSION = "2999.01.01.01.01";

    @Test
    public void refuseDbMigratedByNewerLinstor() throws Exception
    {
        DbMigrater migrater = new DbMigrater(new EmptyErrorReporter());
        DatabaseDriverInfo dbInfo = DatabaseDriverInfo.createDriverInfo("h2");
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:dbMigraterTest"))
        {
            migrater.setSchema(conn, dbInfo);
            try (PreparedStatement stmt = conn.prepareStatement(dbInfo.versionTableInsertStatement()))
            {
                stmt.setInt(1, 1);
                stmt.setString(2, FUTURE_MIGRATION_VERSION);
                stmt.setString(3, "Migration of a newer LINSTOR version");
                stmt.setString(4, "com.example.FutureMigration");
                stmt.setInt(5, 0);
                stmt.execute();
            }
            conn.commit();

            assertThatThrownBy(() -> migrater.migrateToVersion(conn, dbInfo, null))
                .isInstanceOf(InitializationException.class)
                .hasMessageContaining("newer")
                .hasMessageContaining(FUTURE_MIGRATION_VERSION);
        }
    }
}
