package com.linbit.linstor.dbcp.migration.k8s.crd;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.dbdrivers.k8s.crd.GenCrdV1_33_1;
import com.linbit.linstor.dbdrivers.k8s.crd.GenCrdV1_34_0;

@K8sCrdMigration(
    description = "Drop security tables",
    version = 35
)
public class Migration_35_v1_34_0_DropSecurityTables extends BaseK8sCrdMigration
{
    public Migration_35_v1_34_0_DropSecurityTables()
    {
        super(
            GenCrdV1_33_1.createMigrationContext(),
            GenCrdV1_34_0.createMigrationContext()
        );
    }

    @Override
    public @Nullable MigrationResult migrateImpl(MigrationContext migrationCtxRef) throws Exception
    {
        // update CRD entries for all DatabaseTables
        updateCrdSchemaForAllTables();

        return null;
    }
}
