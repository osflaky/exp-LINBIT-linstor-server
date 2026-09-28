package com.linbit.linstor.netcom;

/**
 * Thrown when a message is in an illegal state for the requested operation.
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class IllegalMessageStateException extends Exception
{
    public IllegalMessageStateException()
    {
    }

    public IllegalMessageStateException(String message)
    {
        super(message);
    }

    public IllegalMessageStateException(Throwable cause)
    {
        super(cause);
    }

    public IllegalMessageStateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
