package com.linbit.linstor.dbdrivers;

public interface DatabaseTable
{
    /**
     * Returns all columns of the current table
     */
    Column[] values();

    /**
     * Returns the name of the current table
     */
    String getName();

    public interface Column
    {
        String getName();

        int getSqlType();

        boolean isPk();

        boolean isNullable();

        DatabaseTable getTable();
    }

    /**
     * Describes a self-referencing foreign key: within the table <code>tableName</code>, the column
     * <code>referencingClmName</code> references the column <code>referencedClmName</code> of another entry
     * of the same table. Example: RESOURCE_DEFINITIONS contains resource-definitions as well as
     * snapshot-definitions, where a snapshot-definition references its resource-definition via
     * PARENT_UUID -> UUID.
     */
    record SelfReferencingForeignKey(String tableName, String referencedClmName, String referencingClmName)
    {
    }
}
