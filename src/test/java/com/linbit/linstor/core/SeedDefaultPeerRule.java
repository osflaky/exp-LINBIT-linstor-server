package com.linbit.linstor.core;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SeedDefaultPeerRule implements TestRule
{
    private boolean seedDefaultPeer = true;

    public boolean shouldSeedDefaultPeer()
    {
        return seedDefaultPeer;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        if (description.getAnnotation(DoNotSeedDefaultPeer.class) != null)
        {
            seedDefaultPeer = false;
        }
        return base;
    }
}
