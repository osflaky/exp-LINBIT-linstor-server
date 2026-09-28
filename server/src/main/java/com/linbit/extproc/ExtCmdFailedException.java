package com.linbit.extproc;

import com.linbit.ChildProcessTimeoutException;
import com.linbit.extproc.ExtCmd.OutputData;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.annotation.Nullable;
import com.linbit.utils.ShellUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExtCmdFailedException extends LinStorException
{
    private static final long serialVersionUID = 5779506237459279868L;

    private static final String EXCEPTION_DESCR_FORMAT = "Execution of the external command '%s' failed.";
    private static final String EXCEPTION_DETAILS_FORMAT = "The full command line executed was:\n%s";
    private static final String EXCEPTION_STDOUT_DATA = "The external command sent the following output data:";
    private static final String EXCEPTION_STDERR_DATA = "The external command sent the following error information:";

    /**
     * Null if a timeout or IOException happened.
     */
    private final transient @Nullable OutputData outputData;

    public ExtCmdFailedException(String[] command, ChildProcessTimeoutException cause)
    {
        super(
            String.format(
                "The external command '%s' did not complete within the timeout%s",
                command[0],
                waitedSecondsText(cause, " (waited %s seconds)")
            ),
            String.format(EXCEPTION_DESCR_FORMAT, command[0]),
            """
            The external command did not complete within the timeout.
            Possible causes include:
            - The system load may be too high to ensure completion of external commands in a timely manner.
            - The program implementing the external command may not be operating properly.
            - The operating system may have entered an erroneous state.""",
            """
            Check whether the external program and the operating system are still operating properly.
            Check whether the system's load is within normal parameters.
            """,
            String.format(EXCEPTION_DETAILS_FORMAT, ShellUtils.joinShellQuote(command)) +
                waitedSecondsText(cause, "\nThe command was given up on after waiting %s seconds."),
            cause
        );
        outputData = null;
    }

    /**
     * {@code format} with the waited seconds filled in, or "" when the cause does not know
     * how long was waited. Seconds rather than ms, as requested in the issue: an operator
     * skimming an ErrorReport compares this against timeouts that are configured in seconds.
     */
    private static String waitedSecondsText(ChildProcessTimeoutException cause, String format)
    {
        long waitedMs = cause.getWaitedTimeMs();
        String ret = "";
        if (waitedMs >= 0)
        {
            // manual formatting instead of "%.1f": String.format is locale dependent and an
            // ErrorReport should not switch between "45.5" and "45,5" with the system locale
            ret = String.format(format, waitedMs / 1000 + "." + waitedMs % 1000 / 100);
        }
        return ret;
    }

    public ExtCmdFailedException(String[] command, IOException cause)
    {
        super(
            String.format("Data exchange with the external command '%s' failed", command[0]),
            String.format(EXCEPTION_DESCR_FORMAT, command[0]),
            "Data exchange with the external command failed before the execution completed, or " +
            "the amount of data sent by the external command exceeded the size limit.",
            "Check whether the external program is operating properly and produces meaningful output.",
            String.format(EXCEPTION_DETAILS_FORMAT, ShellUtils.joinShellQuote(command)),
            cause
        );
        outputData = null;
    }

    public ExtCmdFailedException(String[] command, OutputData outputDataRef)
    {
        super(
            String.format("The external command '%s' exited with error code %d\n", command[0], outputDataRef.exitCode),
            String.format(EXCEPTION_DESCR_FORMAT, command[0]),
            String.format("The external command exited with error code %d.", outputDataRef.exitCode),
            """
            - Check whether the external program is operating properly.
            - Check whether the command line is correct.
              Contact a system administrator or a developer if the command line is no longer valid
              for the installed version of the external program.""",
            String.format(
                EXCEPTION_DETAILS_FORMAT +
                "\n\n",
                ShellUtils.joinShellQuote(command)
            ) +
                EXCEPTION_STDOUT_DATA + "\n" + new String(outputDataRef.stdoutData, StandardCharsets.UTF_8) + "\n\n" +
                EXCEPTION_STDERR_DATA + "\n" + new String(outputDataRef.stderrData, StandardCharsets.UTF_8) + "\n"
        );
        outputData = outputDataRef;
    }

    public @Nullable OutputData getOutputData()
    {
        return outputData;
    }
}
