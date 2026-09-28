package com.linbit.linstor.core.apicallhandler.controller.autoplacer;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Thrown by the autoplacer / {@link SelectionManager} when it cannot satisfy a selection rule because the
 * already-deployed resources have put that rule into a self-contradictory state - for example a
 * {@code --replicas-on-same}  property that already carries conflicting values across the deployed nodes, so the
 * autoplacer cannot decide which single value to continue with.
 *
 * <p>Unlike a plain {@link ApiRcException}, this carries the structured list of conflicting rules
 * (rule type + exact property key + the conflicting values). A caller that knows how to cope - e.g.
 * {@code CtrlRscAutoTieBreakerHelper}, which may relax {@code --replicas-on-same} and retry the
 * placement - can inspect those conflicts and react accordingly instead of failing the whole
 * operation.</p>
 *
 * <p>It still extends {@link ApiRcException}, so any caller that does not care about the extra detail
 * keeps treating it exactly as before.</p>
 */
public class SelectionException extends ApiRcException
{
    private static final long serialVersionUID = 8451632405321486610L;

    public enum RuleType
    {
        REPLICAS_ON_SAME("--replicas-on-same");

        private final String displayName;

        RuleType(String displayNameRef)
        {
            displayName = displayNameRef;
        }

        public String getDisplayName()
        {
            return displayName;
        }
    }

    /**
     * A single conflicting rule: which rule type is in conflict, the exact property key and the
     * distinct values found on the already-deployed nodes.
     */
    public static final class Conflict
    {
        private final RuleType ruleType;
        private final String key;
        private final Set<String> conflictingValues;

        public Conflict(RuleType ruleTypeRef, String keyRef, Collection<String> conflictingValuesRef)
        {
            ruleType = ruleTypeRef;
            key = keyRef;
            conflictingValues = Collections.unmodifiableSet(new LinkedHashSet<>(conflictingValuesRef));
        }

        public RuleType getRuleType()
        {
            return ruleType;
        }

        public String getKey()
        {
            return key;
        }

        public Set<String> getConflictingValues()
        {
            return conflictingValues;
        }

        public String describe()
        {
            return switch (ruleType)
            {
                case REPLICAS_ON_SAME -> "The property in " + ruleType.getDisplayName() + " '" + key +
                    "' is already set on already deployed nodes with different values. Autoplacer cannot decide " +
                    "which value to continue with. Linstor found the following conflicting values: " +
                    conflictingValues;
            };
        }
    }

    private final transient List<Conflict> conflicts;

    private SelectionException(ApiCallRc.RcEntry rcEntryRef, List<Conflict> conflictsRef)
    {
        super(rcEntryRef);
        conflicts = Collections.unmodifiableList(new ArrayList<>(conflictsRef));
    }

    public List<Conflict> getConflicts()
    {
        return conflicts;
    }

    /**
     * Returns the exact property keys that are in conflict for the given rule type (never null).
     */
    public Set<String> getConflictingKeys(RuleType ruleTypeRef)
    {
        Set<String> ret = new LinkedHashSet<>();
        for (Conflict conflict : conflicts)
        {
            if (conflict.ruleType == ruleTypeRef)
            {
                ret.add(conflict.key);
            }
        }
        return ret;
    }

    /**
     * Builds a {@link SelectionException} for one or more {@code --replicas-on-same} properties that
     * already carry conflicting values on the already-deployed nodes.
     */
    public static SelectionException replicasOnSameConflicts(List<Conflict> conflictsRef)
    {
        return new SelectionException(buildRcEntry(conflictsRef), conflictsRef);
    }

    private static ApiCallRc.RcEntry buildRcEntry(List<Conflict> conflictsRef)
    {
        final String message;
        if (conflictsRef.size() == 1)
        {
            Conflict conflict = conflictsRef.get(0);
            // keep the original wording for the common single-conflict case
            message = conflict.describe();
        }
        else
        {
            StringBuilder sb = new StringBuilder(
                "Multiple properties are already set on already deployed nodes with different values, so the " +
                    "autoplacer cannot decide which value to continue with:"
            );
            for (Conflict conflict : conflictsRef)
            {
                sb.append("\n  * ").append(conflict.describe());
            }
            message = sb.toString();
        }
        return ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_UNDECIDABLE_AUTOPLACMENT, message);
    }
}
