package com.linbit.linstor.logging;

import com.linbit.linstor.dbdrivers.DatabaseException;

import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import static org.junit.Assert.fail;

public class ChangeLogLevelTest
{
    private static StdErrorReporter reporter;

    @BeforeClass
    public static void setUpClass()
    {
        reporter = new StdErrorReporter(
            "test",
            Paths.get(
                // do not use "." otherwise we keep getting created an error-report.mv.db in the project root dir
                "build/test-logs/changeLogLevel"
            ),
            true,
            "node",
            "INFO",
            "DEBUG"
        );
        reporter = Mockito.spy(reporter);
        Mockito.doAnswer(invoc ->
        {
            fail();
            return null;
        })
            .when(reporter)
            .logError(Mockito.anyString(), Mockito.any());
    }

    @AfterClass
    public static void tearDownClass() throws DatabaseException
    {
        reporter.shutdown();
    }

    @Test
    public void testChangeLogLevel() throws Exception
    {
        reporter.setLogLevel(Level.DEBUG, Level.ERROR);
        reporter.setLogLevel(Level.INFO, null);
        reporter.setLogLevel(null, Level.TRACE);
        reporter.setLogLevel(null, null);
    }
}
