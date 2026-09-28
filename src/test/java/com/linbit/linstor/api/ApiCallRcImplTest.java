package com.linbit.linstor.api;

import com.linbit.ImplementationError;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.api.ApiCallRcImpl.ApiCallRcEntry;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApiCallRcImplTest
{
    @Test
    public void simpleEntryBasics()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "db failed");

        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_SQL);
        assertThat(entry.getMessage()).isEqualTo("db failed");
        assertThat(entry.getCause()).isNull();
        assertThat(entry.getCorrection()).isNull();
        assertThat(entry.getDetails()).isNull();
        assertThat(entry.getObjRefs()).isEmpty();
        assertThat(entry.getErrorIds()).isEmpty();
        assertThat(entry.isError()).isTrue();
        assertThat(entry.skipErrorReport()).isFalse();
        assertThat(entry.appendObjectDescrptionToDetails()).isTrue();
        assertThat(entry.getDateTime()).isNotNull();
    }

    @Test
    public void simpleEntryWithSkipErrorReport()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "db failed", true);
        assertThat(entry.skipErrorReport()).isTrue();
    }

    @Test
    public void simpleEntryWithCause()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "db failed", "disk on fire");
        assertThat(entry.getMessage()).isEqualTo("db failed");
        assertThat(entry.getCause()).isEqualTo("disk on fire");
    }

    @Test
    public void skipErrorReportIsAlwaysTrueForNonErrorEntries()
    {
        // characterization: skipErrorReport() returns "!isError() || skipErrorReport"
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "created", false);
        assertThat(entry.isError()).isFalse();
        assertThat(entry.skipErrorReport()).isTrue();
    }

    @Test
    public void entryBuilderSetsAllFields()
    {
        Map<String, String> moreObjRefs = new HashMap<>();
        moreObjRefs.put(ApiConsts.KEY_RSC_DFN, "rsc1");

        ApiCallRcEntry entry = ApiCallRcImpl
            .entryBuilder(ApiConsts.FAIL_NOT_FOUND_NODE, "node not found")
            .setCause("no such node")
            .setCorrection("create the node first")
            .setDetails("looked everywhere")
            .putObjRef(ApiConsts.KEY_NODE, "node1")
            .putAllObjRefs(moreObjRefs)
            .addErrorId("ERR-1")
            .addAllErrorIds(Arrays.asList("ERR-2", "ERR-3"))
            .setSkipErrorReport(true)
            .setAppendObjectDescriptionToDetails(false)
            .build();

        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_NOT_FOUND_NODE);
        assertThat(entry.getMessage()).isEqualTo("node not found");
        assertThat(entry.getCause()).isEqualTo("no such node");
        assertThat(entry.getCorrection()).isEqualTo("create the node first");
        assertThat(entry.getDetails()).isEqualTo("looked everywhere");
        assertThat(entry.getObjRefs())
            .containsEntry(ApiConsts.KEY_NODE, "node1")
            .containsEntry(ApiConsts.KEY_RSC_DFN, "rsc1")
            .hasSize(2);
        assertThat(entry.getErrorIds()).containsExactly("ERR-1", "ERR-2", "ERR-3");
        assertThat(entry.skipErrorReport()).isTrue();
        assertThat(entry.appendObjectDescrptionToDetails()).isFalse();
    }

    @Test
    public void entryBuilderIgnoresNullErrorId()
    {
        ApiCallRcEntry entry = ApiCallRcImpl
            .entryBuilder(ApiConsts.FAIL_SQL, "msg")
            .addErrorId(null)
            .build();
        assertThat(entry.getErrorIds()).isEmpty();
    }

    @Test
    public void entryBuilderFromSourceCopiesAllFields()
    {
        ApiCallRcEntry source = ApiCallRcImpl
            .entryBuilder(ApiConsts.FAIL_NOT_FOUND_NODE, "original message")
            .setCause("cause")
            .setCorrection("correction")
            .setDetails("details")
            .putObjRef(ApiConsts.KEY_NODE, "node1")
            .addErrorId("ERR-1")
            .setSkipErrorReport(true)
            .setAppendObjectDescriptionToDetails(false)
            .build();

        ApiCallRcEntry copy = ApiCallRcImpl.entryBuilder(source, null, null).build();

        assertThat(copy.getReturnCode()).isEqualTo(ApiConsts.FAIL_NOT_FOUND_NODE);
        assertThat(copy.getMessage()).isEqualTo("original message");
        assertThat(copy.getCause()).isEqualTo("cause");
        assertThat(copy.getCorrection()).isEqualTo("correction");
        assertThat(copy.getDetails()).isEqualTo("details");
        assertThat(copy.getObjRefs()).containsExactlyEntriesOf(source.getObjRefs());
        assertThat(copy.getErrorIds()).containsExactly("ERR-1");
        assertThat(copy.skipErrorReport()).isTrue();
        assertThat(copy.appendObjectDescrptionToDetails()).isFalse();
    }

    @Test
    public void entryBuilderFromSourceAppliesOverrides()
    {
        ApiCallRcEntry source = ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "original message");

        ApiCallRcEntry copy = ApiCallRcImpl
            .entryBuilder(source, ApiConsts.FAIL_SQL_ROLLBACK, "new message")
            .build();

        assertThat(copy.getReturnCode()).isEqualTo(ApiConsts.FAIL_SQL_ROLLBACK);
        assertThat(copy.getMessage()).isEqualTo("new message");
    }

    @Test
    public void severityIsDerivedFromTypeBits()
    {
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "").getSeverity())
            .isEqualTo(ApiCallRc.Severity.ERROR);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.WARN_NOT_CONNECTED, "").getSeverity())
            .isEqualTo(ApiCallRc.Severity.WARNING);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_INFO | 1L, "").getSeverity())
            .isEqualTo(ApiCallRc.Severity.INFO);
        // success codes also map to INFO
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "").getSeverity())
            .isEqualTo(ApiCallRc.Severity.INFO);
    }

    @Test
    public void actionIsDerivedFromOpBits()
    {
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_CRT | 1L, "").getAction())
            .isEqualTo(ApiCallRc.Action.CREATE);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_MOD | 1L, "").getAction())
            .isEqualTo(ApiCallRc.Action.MODIFY);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_DEL | 1L, "").getAction())
            .isEqualTo(ApiCallRc.Action.DELETE);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "").getAction())
            .isEqualTo(ApiCallRc.Action.UNKNOWN);
    }

    @Test
    public void objectsAreDerivedFromObjBits()
    {
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL | ApiConsts.MASK_NODE, "").getObjects())
            .containsExactly(ApiCallRc.LinstorObj.NODE);
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_CRT | ApiConsts.MASK_BACKUP, "").getObjects())
            .containsExactly(ApiCallRc.LinstorObj.BACKUP);
        // no object bits set
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "").getObjects()).isEmpty();
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void errorCodeMasksTypeOpAndObjBits()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(
            ApiConsts.FAIL_INVLD_NODE_NAME | ApiConsts.MASK_CRT | ApiConsts.MASK_NODE,
            ""
        );
        // FAIL_INVLD_NODE_NAME is defined as 200 | MASK_ERROR
        assertThat(entry.getErrorCode()).isEqualTo(200L);
    }

    @Test
    public void isErrorOnlySetForErrorMask()
    {
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "").isError()).isTrue();
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.WARN_NOT_CONNECTED, "").isError()).isFalse();
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.MASK_INFO | 1L, "").isError()).isFalse();
        assertThat(ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "").isError()).isFalse();
    }

    @Test
    public void setReturnCodeBitAccumulatesBits()
    {
        ApiCallRcEntry entry = new ApiCallRcEntry();
        entry.setReturnCode(ApiConsts.FAIL_SQL);
        entry.setReturnCodeBit(ApiConsts.MASK_NODE);
        entry.setReturnCodeBit(ApiConsts.MASK_CRT);
        assertThat(entry.getReturnCode())
            .isEqualTo(ApiConsts.FAIL_SQL | ApiConsts.MASK_NODE | ApiConsts.MASK_CRT);
    }

    @Test
    public void entrySettersAreChainableAndApplied()
    {
        ApiCallRcEntry entry = new ApiCallRcEntry()
            .setReturnCode(ApiConsts.FAIL_SQL)
            .setMessage("msg")
            .setCause("cause")
            .setCorrection("correction")
            .setDetails("details")
            .putObjRef(ApiConsts.KEY_NODE, "node1")
            .addErrorId("ERR-1")
            .addAllErrorIds(new TreeSet<>(Collections.singleton("ERR-2")))
            .setSkipErrorReport(true)
            .setAppendObjectDescriptionToDetails(false);

        assertThat(entry.getMessage()).isEqualTo("msg");
        assertThat(entry.getCause()).isEqualTo("cause");
        assertThat(entry.getCorrection()).isEqualTo("correction");
        assertThat(entry.getDetails()).isEqualTo("details");
        assertThat(entry.getObjRefs()).containsEntry(ApiConsts.KEY_NODE, "node1");
        assertThat(entry.getErrorIds()).containsExactly("ERR-1", "ERR-2");
        assertThat(entry.skipErrorReport()).isTrue();
        assertThat(entry.appendObjectDescrptionToDetails()).isFalse();
    }

    @Test
    public void addEntryIgnoresNull()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        apiCallRc.addEntry(null);
        assertThat(apiCallRc).isEmpty();
    }

    @Test
    public void addEntryWithMessageAndReturnCode()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        apiCallRc.addEntry("hello", ApiConsts.CREATED);
        assertThat(apiCallRc).hasSize(1);
        assertThat(apiCallRc.get(0).getMessage()).isEqualTo("hello");
        assertThat(apiCallRc.get(0).getReturnCode()).isEqualTo(ApiConsts.CREATED);
    }

    @Test
    public void addEntriesAppendsAllEntriesOfOtherApiCallRc()
    {
        ApiCallRcImpl first = new ApiCallRcImpl(ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "one"));
        ApiCallRcImpl second = new ApiCallRcImpl();
        second.addEntry("two", ApiConsts.MODIFIED);
        second.addEntry("three", ApiConsts.DELETED);

        first.addEntries(second);

        assertThat(first).hasSize(3);
        assertThat(first.get(1).getMessage()).isEqualTo("two");
        assertThat(first.get(2).getMessage()).isEqualTo("three");
    }

    @Test
    public void hasErrorsOnlyConsidersErrorEntries()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        assertThat(apiCallRc.hasErrors()).isFalse();

        apiCallRc.addEntry("warn", ApiConsts.WARN_NOT_CONNECTED);
        assertThat(apiCallRc.hasErrors()).isFalse();

        apiCallRc.addEntry("error", ApiConsts.FAIL_SQL);
        assertThat(apiCallRc.hasErrors()).isTrue();
    }

    @Test
    public void allSkipErrorReportOnlyConsidersErrorEntries()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        // empty: allMatch on empty stream
        assertThat(apiCallRc.allSkipErrorReport()).isTrue();

        // non-error entries are filtered out entirely
        apiCallRc.addEntry("info", ApiConsts.MASK_INFO | 1L);
        assertThat(apiCallRc.allSkipErrorReport()).isTrue();

        apiCallRc.addEntry(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "skipped error", true));
        assertThat(apiCallRc.allSkipErrorReport()).isTrue();

        apiCallRc.addEntry(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "reported error", false));
        assertThat(apiCallRc.allSkipErrorReport()).isFalse();
    }

    @Test
    public void singletonAndSingleApiCallRcFactories()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "created");
        ApiCallRcImpl single = ApiCallRcImpl.singletonApiCallRc(entry);
        assertThat(single).containsExactly(entry);

        ApiCallRcImpl fromCode = ApiCallRcImpl.singleApiCallRc(ApiConsts.FAIL_SQL, "boom");
        assertThat(fromCode).hasSize(1);
        assertThat(fromCode.get(0).getReturnCode()).isEqualTo(ApiConsts.FAIL_SQL);
        assertThat(fromCode.get(0).getMessage()).isEqualTo("boom");

        ApiCallRcImpl withCause = ApiCallRcImpl.singleApiCallRc(ApiConsts.FAIL_SQL, "boom", "cause");
        assertThat(withCause.get(0).getCause()).isEqualTo("cause");
    }

    @Test
    public void copyFromLinstorExcUsesPlainMessageWithoutDescription()
    {
        LinStorException exc = new LinStorException("plain message");
        ApiCallRc.RcEntry entry = ApiCallRcImpl.copyFromLinstorExc(ApiConsts.FAIL_SQL, exc);

        assertThat(entry.getReturnCode()).isEqualTo(ApiConsts.FAIL_SQL);
        assertThat(entry.getMessage()).isEqualTo("plain message");
        assertThat(entry.getCause()).isNull();
        assertThat(entry.getCorrection()).isNull();
        assertThat(entry.getDetails()).isNull();
    }

    @Test
    public void copyFromLinstorExcPrefersDescriptionTexts()
    {
        LinStorException exc = new LinStorException(
            "internal message",
            "description",
            "cause text",
            "correction text",
            "details text"
        );
        ApiCallRc.RcEntry entry = ApiCallRcImpl.copyFromLinstorExc(ApiConsts.FAIL_SQL, exc);

        assertThat(entry.getMessage()).isEqualTo("description");
        assertThat(entry.getCause()).isEqualTo("cause text");
        assertThat(entry.getCorrection()).isEqualTo("correction text");
        assertThat(entry.getDetails()).isEqualTo("details text");
    }

    @Test
    public void copyAndPrefixMessageCopiesFields()
    {
        ApiCallRcEntry source = ApiCallRcImpl
            .entryBuilder(ApiConsts.FAIL_SQL, "original")
            .setCause("cause")
            .setCorrection("correction")
            .setDetails("details")
            .putObjRef(ApiConsts.KEY_NODE, "node1")
            .addErrorId("ERR-1")
            .build();

        ApiCallRcEntry copy = ApiCallRcImpl.copyAndPrefixMessage("prefix: ", source);

        assertThat(copy.getReturnCode()).isEqualTo(ApiConsts.FAIL_SQL);
        assertThat(copy.getMessage()).isEqualTo("prefix: original");
        assertThat(copy.getCause()).isEqualTo("cause");
        assertThat(copy.getCorrection()).isEqualTo("correction");
        assertThat(copy.getDetails()).isEqualTo("details");
        assertThat(copy.getObjRefs()).containsEntry(ApiConsts.KEY_NODE, "node1");
        assertThat(copy.getErrorIds()).containsExactly("ERR-1");
        assertThat(copy.skipErrorReport()).isFalse();
    }

    @Test
    public void copyAndPrefixPrefixesAllEntries()
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        apiCallRc.addEntry("one", ApiConsts.CREATED);
        apiCallRc.addEntry("two", ApiConsts.DELETED);

        ApiCallRcImpl prefixed = ApiCallRcImpl.copyAndPrefix("p: ", apiCallRc);

        assertThat(prefixed).hasSize(2);
        assertThat(prefixed.get(0).getMessage()).isEqualTo("p: one");
        assertThat(prefixed.get(1).getMessage()).isEqualTo("p: two");
        // original remains untouched
        assertThat(apiCallRc.get(0).getMessage()).isEqualTo("one");
    }

    @Test
    public void listBehavior()
    {
        ApiCallRcEntry entryA = ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "a");
        ApiCallRcEntry entryB = ApiCallRcImpl.simpleEntry(ApiConsts.DELETED, "b");
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl(Arrays.asList(entryA, entryB));

        assertThat(apiCallRc.size()).isEqualTo(2);
        assertThat(apiCallRc.isEmpty()).isFalse();
        assertThat(apiCallRc.get(0)).isSameAs(entryA);
        assertThat(apiCallRc.contains(entryB)).isTrue();
        assertThat(apiCallRc.indexOf(entryB)).isEqualTo(1);
        assertThat(apiCallRc.lastIndexOf(entryA)).isZero();
        assertThat(apiCallRc.subList(1, 2)).containsExactly(entryB);
        assertThat(apiCallRc.toArray()).containsExactly(entryA, entryB);

        ApiCallRcEntry entryC = ApiCallRcImpl.simpleEntry(ApiConsts.MODIFIED, "c");
        assertThat(apiCallRc.add(entryC)).isTrue();
        apiCallRc.add(0, ApiCallRcImpl.simpleEntry(ApiConsts.MODIFIED, "d"));
        assertThat(apiCallRc.get(0).getMessage()).isEqualTo("d");
        assertThat(apiCallRc.addAll(Collections.singletonList(entryA))).isTrue();
        assertThat(apiCallRc).hasSize(5);
    }

    @Test
    public void mutatingListOperationsAreForbidden()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.CREATED, "a");
        ApiCallRcImpl apiCallRc = ApiCallRcImpl.singletonApiCallRc(entry);

        assertThatThrownBy(() -> apiCallRc.replaceAll(rcEntry -> rcEntry))
            .isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.sort(null)).isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.remove(entry)).isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.remove(0)).isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.removeAll(Collections.singletonList(entry)))
            .isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.retainAll(Collections.emptyList()))
            .isInstanceOf(ImplementationError.class);
        assertThatThrownBy(apiCallRc::clear).isInstanceOf(ImplementationError.class);
        assertThatThrownBy(() -> apiCallRc.set(0, entry)).isInstanceOf(ImplementationError.class);
    }

    @Test
    public void toStringContainsEntryData()
    {
        ApiCallRcEntry entry = ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_SQL, "boom");
        assertThat(entry.toString())
            .contains("message='boom'")
            .contains("FAIL_SQL");

        ApiCallRcImpl apiCallRc = ApiCallRcImpl.singletonApiCallRc(entry);
        assertThat(apiCallRc.toString()).startsWith("ApiCallRcImpl{");
    }
}
