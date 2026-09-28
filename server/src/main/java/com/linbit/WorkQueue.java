package com.linbit;

/**
 * A queue for submitting work items for asynchronous execution.
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface WorkQueue
{
    /**
     * Submits a Runnable object to the work queue for (possibly asynchronous) execution.
     *
     * @param task The Runnable to execute
     */
    void submit(Runnable task);
}
