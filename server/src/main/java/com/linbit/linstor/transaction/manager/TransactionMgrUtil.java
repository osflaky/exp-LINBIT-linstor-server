package com.linbit.linstor.transaction.manager;

import com.linbit.linstor.api.LinStorScope;

public class TransactionMgrUtil
{
    public static void seedTransactionMgr(final LinStorScope initScope, final TransactionMgr transMgr)
    {
        initScope.seed(TransactionMgr.class, transMgr);
        if (transMgr instanceof TransactionMgrSQL transactionMgrSQL)
        {
            initScope.seed(TransactionMgrSQL.class, transactionMgrSQL);
        }
        else
        if (transMgr instanceof TransactionMgrK8sCrd transactionMgrK8sCrd)
        {
            initScope.seed(TransactionMgrK8sCrd.class, transactionMgrK8sCrd);
        }
        else
        if (transMgr instanceof SatelliteTransactionMgr)
        {

        }
        else
        {
            // TODO report error instead runtimeExc
            throw new RuntimeException("Not implemented");
        }
    }

    private TransactionMgrUtil()
    {
    }
}
