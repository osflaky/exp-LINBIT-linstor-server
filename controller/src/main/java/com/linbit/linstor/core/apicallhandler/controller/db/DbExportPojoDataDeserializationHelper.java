package com.linbit.linstor.core.apicallhandler.controller.db;

import com.linbit.ImplementationError;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.core.ClassPathLoader;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.dbdrivers.DatabaseTable;
import com.linbit.linstor.dbdrivers.k8s.crd.GenCrd;
import com.linbit.linstor.dbdrivers.k8s.crd.GenCrdCurrent;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorCrd;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorData;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorSpec;
import com.linbit.linstor.logging.ErrorReporter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * This class provides Jackson deserializers for {@link LinstorSpec} as well as for {@link DbExportPojoData.Table}
 * instances.
 * The reason for this "combined" deserializer is that during parsing the table's name is stored as currentTable in this
 * class. That reference is used by the LinstorSpec deserializer to tell Jackson to which instance the JSON should be
 * deserialized into.
 */
public class DbExportPojoDataDeserializationHelper
{
    private static final String NESTED_GEN_DB_TABLES_CLASS_NAME = "GeneratedDatabaseTables";
    private static final String SELF_REF_FKS_FIELD_NAME = "SELF_REFERENCING_FOREIGN_KEYS";

    private @Nullable String currentTable = null;

    private final Map<String, Class<? extends LinstorSpec<?, ?>>> jsonLinstorSpecMapping;
    private final Map<String, Class<? extends LinstorCrd<?>>> jsonLinstorCrdMapping;
    private final LinstorSpecDeserializer linstorSpecDeserializer = new LinstorSpecDeserializer();
    private final TableDeserializer dbExportTableDeserializer = new TableDeserializer();
    private final String genCrdVersion;
    private @Nullable Class<?> matchedGenCrdCls = null;

    /**
     * Initializes the two deserializers as well as search for the correct GenCrd* class, specified by the string
     * parameter
     *
     */
    @SuppressWarnings("unchecked")
    public DbExportPojoDataDeserializationHelper(ErrorReporter errorReporterRef, String genCrdVersionRef)
    {
        genCrdVersion = genCrdVersionRef;

        ClassPathLoader classPathLoader = new ClassPathLoader(errorReporterRef);
        List<Class<?>> genCrdClasses = classPathLoader.loadClasses(
            GenCrdCurrent.class.getPackage().getName(),
            Collections.singletonList(""),
            null,
            GenCrd.class
        );
        jsonLinstorSpecMapping = new HashMap<>();
        jsonLinstorCrdMapping = new HashMap<>();
        for (Class<?> genCrdCls : genCrdClasses)
        {
            GenCrd annot = genCrdCls.getAnnotation(GenCrd.class);
            if (genCrdCls != GenCrdCurrent.class && annot != null && annot.dataVersion().equals(genCrdVersionRef))
            {
                matchedGenCrdCls = genCrdCls;
                HashMap<String, String> expectedLowerCaseCrdClassNamesToDbTblName = new HashMap<>();

                for (Class<?> declaredLinstorSpecCls : genCrdCls.getDeclaredClasses())
                {
                    LinstorData linstorDataAnnot = declaredLinstorSpecCls.getAnnotation(LinstorData.class);
                    if (linstorDataAnnot != null)
                    {
                        jsonLinstorSpecMapping.put(
                            linstorDataAnnot.tableName(),
                            (Class<? extends LinstorSpec<?, ?>>) declaredLinstorSpecCls
                        );

                        String simpleName = declaredLinstorSpecCls.getSimpleName();
                        expectedLowerCaseCrdClassNamesToDbTblName.put(
                            simpleName.substring(0, simpleName.length() - "Spec".length()).toLowerCase(),
                            linstorDataAnnot.tableName()
                        );
                    }
                }

                for (Class<?> declaredLinstorCrdCls : genCrdCls.getDeclaredClasses())
                {
                    String dbTblName = expectedLowerCaseCrdClassNamesToDbTblName.get(
                        declaredLinstorCrdCls.getSimpleName().toLowerCase()
                    );
                    if (dbTblName != null)
                    {
                        jsonLinstorCrdMapping.put(
                            dbTblName,
                            (Class<? extends LinstorCrd<?>>) declaredLinstorCrdCls
                        );
                    }
                }

                break;
            }
        }
    }

    public TableDeserializer getDbExportTableDeserializer()
    {
        return dbExportTableDeserializer;
    }

    /**
     * Returns the self-referencing foreign keys of the database schema version this helper was created for,
     * taken from the SELF_REFERENCING_FOREIGN_KEYS constant of the nested GeneratedDatabaseTables copy of the
     * corresponding GenCrdV* class.
     */
    public DatabaseTable.SelfReferencingForeignKey[] getSelfReferencingForeignKeys() throws DatabaseException
    {
        if (matchedGenCrdCls == null)
        {
            throw new DatabaseException(
                "No GenCrd* class found for the database export's data version: " + genCrdVersion
            );
        }
        @Nullable Class<?> genDbTablesCls = null;
        for (Class<?> declaredCls : matchedGenCrdCls.getDeclaredClasses())
        {
            if (declaredCls.getSimpleName().equals(NESTED_GEN_DB_TABLES_CLASS_NAME))
            {
                genDbTablesCls = declaredCls;
                break;
            }
        }
        if (genDbTablesCls == null)
        {
            throw new ImplementationError(
                matchedGenCrdCls.getSimpleName() + " does not contain a nested " +
                    NESTED_GEN_DB_TABLES_CLASS_NAME + " class"
            );
        }
        try
        {
            return (DatabaseTable.SelfReferencingForeignKey[]) genDbTablesCls
                .getField(SELF_REF_FKS_FIELD_NAME)
                .get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException exc)
        {
            throw new ImplementationError(
                "Failed to access " + genDbTablesCls.getName() + "." + SELF_REF_FKS_FIELD_NAME +
                    ". Every GenCrdV* class' nested " + NESTED_GEN_DB_TABLES_CLASS_NAME +
                    " class must contain this constant.",
                exc
            );
        }
    }

    public LinstorSpecDeserializer getLinstorSpecDeserializer()
    {
        return linstorSpecDeserializer;
    }

    class LinstorSpecDeserializer extends JsonDeserializer<LinstorSpec<?, ?>>
    {
        @Override
        public LinstorSpec<?, ?> deserialize(JsonParser pRef, DeserializationContext ctxtRef)
            throws IOException
        {
            ObjectCodec codec = pRef.getCodec();
            TreeNode readTree = codec.readTree(pRef);

            Class<? extends LinstorSpec<?, ?>> linstorSpecCls = jsonLinstorSpecMapping.get(currentTable);

            return codec.treeToValue(readTree, linstorSpecCls);
        }
    }

    class TableDeserializer extends JsonDeserializer<DbExportPojoData.Table>
    {
        @Override
        public DbExportPojoData.Table deserialize(JsonParser pRef, DeserializationContext ctxtRef)
            throws IOException
        {
            ObjectCodec codec = pRef.getCodec();
            TreeNode readTree = codec.readTree(pRef);

            TreeNode typeTreeNode = readTree.get(DbExportPojoData.Table.JSON_CLM_NAME);
            currentTable = ((ValueNode) typeTreeNode).asText();

            TreeNode clmDscrSubTree = readTree.get(DbExportPojoData.Table.JSON_CLM_CLM_DESCR);
            JsonParser clmDscrJsonParser = clmDscrSubTree.traverse();
            clmDscrJsonParser.setCodec(codec);
            List<DbExportPojoData.Column> clmDscrList = clmDscrJsonParser.readValueAs(
                new TypeReference<List<DbExportPojoData.Column>>()
            {
            });

            TreeNode dataSubTree = readTree.get(DbExportPojoData.Table.JSON_CLM_DATA);
            JsonParser dataJsonParser = dataSubTree.traverse();
            dataJsonParser.setCodec(codec);
            List<LinstorSpec<?, ?>> dataList = dataJsonParser.readValueAs(
                new TypeReference<List<LinstorSpec<?, ?>>>()
                {
                }
            );

            return new DbExportPojoData.Table(
                currentTable,
                clmDscrList,
                dataList,
                jsonLinstorCrdMapping.get(currentTable)
            );
        }
    }
}
