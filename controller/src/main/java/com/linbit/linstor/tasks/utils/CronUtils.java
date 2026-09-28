package com.linbit.linstor.tasks.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class CronUtils
{
    private static final CronParser CRON_PARSER = new CronParser(
        CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
    );

    private CronUtils()
    {
    }

    public static ZonedDateTime nextExec(String cronRef)
    {
        return nextExec(asCron(cronRef), ZonedDateTime.now(ZoneId.systemDefault()));
    }

    public static ExecutionTime asCron(String cronRef)
    {
        return ExecutionTime.forCron(CRON_PARSER.parse(cronRef));
    }

    public static ZonedDateTime nextExec(ExecutionTime exec)
    {
        return nextExec(exec, ZonedDateTime.now(ZoneId.systemDefault()));
    }

    public static ZonedDateTime nextExec(ExecutionTime exec, ZonedDateTime zdt)
    {
        return exec(exec, zdt, true);
    }

    public static ZonedDateTime lastExec(ExecutionTime exec, ZonedDateTime zdt)
    {
        return exec(exec, zdt, false);
    }

    public static ZonedDateTime exec(ExecutionTime exec, ZonedDateTime zdt, boolean next)
    {
        ZonedDateTime ret;
        if (next)
        {
            ret = exec.nextExecution(zdt).get();
        }
        else
        {
            /*
             * DO NOT use exec.isMatch as that method truncates seconds in CRON_UNIX and CRON4J format
             */
            /*
             * In this else case we are only interested in the previous (last) execution point.
             * However, we have 2 cases: Either zdt is between two execution points or exactly on one exec point.
             * If we are exactly on one execution point, going to the last and afterwards to the next point will
             * result in where we started (zdt).
             * If zdt is between two exec points, we only need .lastExecution() (therefore the "rollback")
             */
            ZonedDateTime tmp = exec.lastExecution(zdt).get();
            ret = exec.nextExecution(tmp).get();
            if (!ret.equals(zdt))
            {
                ret = tmp;
            }
        }
        return ret;
    }
}
