package com.linbit;

public class ChildProcessTimeoutException extends TimeoutException
{
    private static final long UNKNOWN_WAIT_TIME = -1;

    private final boolean termFlag;
    /** How long was actually waited for the child process before giving up, in ms, or -1 when unknown */
    private final long waitedTimeMs;

    public ChildProcessTimeoutException()
    {
        termFlag = false;
        waitedTimeMs = UNKNOWN_WAIT_TIME;
    }

    public ChildProcessTimeoutException(String message)
    {
        super(message);
        termFlag = false;
        waitedTimeMs = UNKNOWN_WAIT_TIME;
    }

    public ChildProcessTimeoutException(String message, boolean terminated)
    {
        super(message);
        termFlag = terminated;
        waitedTimeMs = UNKNOWN_WAIT_TIME;
    }

    public ChildProcessTimeoutException(String message, long waitedTimeMsRef)
    {
        super(message);
        termFlag = false;
        waitedTimeMs = waitedTimeMsRef;
    }

    public ChildProcessTimeoutException(boolean terminated)
    {
        termFlag = terminated;
        waitedTimeMs = UNKNOWN_WAIT_TIME;
    }

    public ChildProcessTimeoutException(boolean terminated, ChildProcessTimeoutException waitTimeoutExcRef)
    {
        // this exception replaces the original in the caller's hands, so whatever the original
        // knew about the wait (message and duration) has to survive the re-wrap
        super(waitTimeoutExcRef.getMessage());
        termFlag = terminated;
        waitedTimeMs = waitTimeoutExcRef.waitedTimeMs;
        addSuppressed(waitTimeoutExcRef);
    }

    public boolean isTerminated()
    {
        return termFlag;
    }

    /**
     * Returns how long was actually waited for the child process before giving up, in ms,
     * or -1 when unknown.
     */
    public long getWaitedTimeMs()
    {
        return waitedTimeMs;
    }
}
