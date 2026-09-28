package com.linbit.linstor.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiRcUtilsTest
{
    @Test
    public void extractResolvesPlainFailCode()
    {
        ApiRcUtils.ResolvedRetCode resolved = ApiRcUtils.extract(ApiConsts.FAIL_INVLD_NODE_NAME);

        assertThat(resolved.lType).isEqualTo(ApiConsts.MASK_ERROR);
        assertThat(resolved.strType).isEqualTo("Error");

        // FAIL_* codes carry neither operation nor object bits
        assertThat(resolved.lOp).isZero();
        assertThat(resolved.strOp).isNull();
        assertThat(resolved.lObj).isZero();
        assertThat(resolved.strObj).isNull();

        assertThat(resolved.lAction).isEqualTo(ApiConsts.FAIL_INVLD_NODE_NAME);
        assertThat(resolved.strAction).isEqualTo("FAIL_INVLD_NODE_NAME");
    }

    @Test
    public void extractResolvesFullyMaskedCode()
    {
        long retCode = ApiConsts.CREATED | ApiConsts.MASK_CRT | ApiConsts.MASK_NODE;
        ApiRcUtils.ResolvedRetCode resolved = ApiRcUtils.extract(retCode);

        assertThat(resolved.strType).isEqualTo("Success");
        assertThat(resolved.lType).isEqualTo(ApiConsts.MASK_SUCCESS);
        assertThat(resolved.strOp).isEqualTo("Create");
        assertThat(resolved.lOp).isEqualTo(ApiConsts.MASK_CRT);
        assertThat(resolved.strObj).isEqualTo("Node");
        assertThat(resolved.lObj).isEqualTo(ApiConsts.MASK_NODE);
        assertThat(resolved.strAction).isEqualTo("Created");
        assertThat(resolved.lAction).isEqualTo(ApiConsts.CREATED);
    }

    @Test
    public void extractResolvesWarnAndDeleteMasks()
    {
        long retCode = ApiConsts.DELETED | ApiConsts.MASK_DEL | ApiConsts.MASK_RSC_DFN;
        ApiRcUtils.ResolvedRetCode resolved = ApiRcUtils.extract(retCode);
        assertThat(resolved.strOp).isEqualTo("Delete");
        assertThat(resolved.strObj).isEqualTo("RscDfn");
        assertThat(resolved.strAction).isEqualTo("Deleted");

        resolved = ApiRcUtils.extract(ApiConsts.WARN_NOT_CONNECTED);
        assertThat(resolved.strType).isEqualTo("Warn");
        assertThat(resolved.strAction).isEqualTo("WARN_NOT_CONNECTED");
    }

    @Test
    public void extractStorPoolDfnMaskIsMappedAsSnapshotDfn()
    {
        // characterization: RET_CODES_OBJ puts "StorPoolDfn" first but overwrites the very same key
        // with "SnapshotDfn" afterwards, so the last mapping wins
        ApiRcUtils.ResolvedRetCode resolved = ApiRcUtils.extract(ApiConsts.MASK_STOR_POOL_DFN);
        assertThat(resolved.strObj).isEqualTo("SnapshotDfn");
    }

    @Test
    public void appendReadableRetCodeWithAllPartsMapped()
    {
        StringBuilder sb = new StringBuilder();
        ApiRcUtils.appendReadableRetCode(sb, ApiConsts.CREATED | ApiConsts.MASK_CRT | ApiConsts.MASK_NODE);
        assertThat(sb.toString()).isEqualTo("Success Create Node Created [13c0001]");
    }

    @Test
    public void appendReadableRetCodeWithUnmappedParts()
    {
        // op and obj bits are not set for FAIL_SQL, StringBuilder renders the misses as "null"
        StringBuilder sb = new StringBuilder();
        ApiRcUtils.appendReadableRetCode(sb, ApiConsts.FAIL_SQL);
        assertThat(sb.toString()).isEqualTo("Error null null FAIL_SQL [c000000000000064]");
    }

    @Test
    public void isErrorDetectsErrorEntries()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        apiCallRc.addEntry("all fine", ApiConsts.CREATED);
        assertThat(ApiRcUtils.isError(apiCallRc)).isFalse();

        apiCallRc.addEntry("boom", ApiConsts.FAIL_SQL);
        assertThat(ApiRcUtils.isError(apiCallRc)).isTrue();
    }

    @Test
    public void isErrorTreatsWarningsAsErrors()
    {
        // characterization: ApiRcUtils.isError also returns true for WARN entries
        // (unlike ApiCallRcImpl.hasErrors which only considers MASK_ERROR)
        ApiCallRcImpl apiCallRc = ApiCallRcImpl.singleApiCallRc(ApiConsts.WARN_NOT_CONNECTED, "warning only");
        assertThat(ApiRcUtils.isError(apiCallRc)).isTrue();
        assertThat(apiCallRc.hasErrors()).isFalse();
    }

    @Test
    public void isErrorIgnoresInfoAndSuccess()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        apiCallRc.addEntry("info", ApiConsts.MASK_INFO | 1L);
        apiCallRc.addEntry("success", ApiConsts.MODIFIED);
        assertThat(ApiRcUtils.isError(apiCallRc)).isFalse();
    }

    @Test
    public void isErrorOnEmptyApiCallRc()
    {
        assertThat(ApiRcUtils.isError(new ApiCallRcImpl())).isFalse();
    }
}
