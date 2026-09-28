package com.linbit.linstor.debug;

import com.linbit.linstor.annotation.Nullable;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

/**
 * Interface for debug commands available in the LINSTOR debug console.
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface CommonDebugCmd
{
    Set<String> getCmdNames();

    @Nullable
    String getDisplayName(String upperCaseCmdName);

    String getCmdInfo();

    String getCmdDescription();

    @Nullable
    Map<String, String> getParametersDescription();

    @Nullable
    String getUndeclaredParametersDescription();

    boolean acceptsUndeclaredParameters();

    boolean requiresScope();

    void execute(
        PrintStream debugOut,
        PrintStream debugErr,
        Map<String, String> parameters
    ) throws Exception;
}
