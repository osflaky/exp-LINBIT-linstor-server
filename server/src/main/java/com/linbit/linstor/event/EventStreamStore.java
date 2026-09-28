package com.linbit.linstor.event;

import com.linbit.linstor.LinStorDataAlreadyExistsException;

import java.util.Collection;

public interface EventStreamStore
{
    void addEventStream(EventIdentifier eventIdentifier)
        throws LinStorDataAlreadyExistsException;

    /**
     * Adds the event stream if it does not already exist.
     *
     * @return True if the event stream did not previously exist.
     */
    boolean addEventStreamIfNew(EventIdentifier eventIdentifier);

    /**
     * Removes the specified event stream.
     *
     * @return True if the event stream previously existed.
     */
    boolean removeEventStream(EventIdentifier eventIdentifier);

    Collection<EventIdentifier> getDescendantEventStreams(EventIdentifier eventIdentifier);
}
