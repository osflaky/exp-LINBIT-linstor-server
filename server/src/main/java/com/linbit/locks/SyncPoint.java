package com.linbit.locks;

/**
 * A synchronization point for coordinating concurrent operations.
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface SyncPoint
{
    void register();
    void arrive();
    void await();
}
