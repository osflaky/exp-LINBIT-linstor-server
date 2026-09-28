package com.linbit.linstor.transaction;

import com.linbit.linstor.dbdrivers.DatabaseDriverInfo;
import com.linbit.linstor.transaction.manager.ControllerSQLTransactionMgrGenerator;
import com.linbit.linstor.transaction.manager.TransactionMgrGenerator;

import com.google.inject.AbstractModule;

public class ControllerTransactionMgrModule extends AbstractModule
{
    private final DatabaseDriverInfo.DatabaseType dbType;

    public ControllerTransactionMgrModule(DatabaseDriverInfo.DatabaseType dbTypeRef)
    {
        dbType = dbTypeRef;
    }

    @Override
    protected void configure()
    {
        bind(TransactionMgrGenerator.class).to(
            switch(dbType)
            {
                case SQL -> ControllerSQLTransactionMgrGenerator.class;
                case K8S_CRD -> ControllerK8sCrdTransactionMgrGenerator.class;
            }
        );
    }
}
