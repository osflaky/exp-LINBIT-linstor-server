package com.linbit.linstor.layer.storage.utils;

import com.linbit.fsevent.FileObserver;
import com.linbit.fsevent.FileSystemWatch;
import com.linbit.fsevent.FileSystemWatch.Event;
import com.linbit.fsevent.FileSystemWatch.FileEntry;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.storage.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeviceUtils
{
    public static void waitUntilDeviceVisible(
        String devicePathStr,
        long waitTimeoutAfterCreateMillis,
        ErrorReporter errorReporterRef,
        FileSystemWatch fsWatchRef
    )
        throws StorageException
    {
        final Object syncObj = new Object();
        FileObserver fileObserver = new FileObserver()
        {
            @Override
            public void fileEvent(FileEntry watchEntry)
            {
                synchronized (syncObj)
                {
                    syncObj.notify();
                }
            }
        };
        try
        {
            synchronized (syncObj)
            {
                long start = System.currentTimeMillis();
                Path devicePath = Paths.get(devicePathStr);
                FileEntry fileWatchEntry = new FileEntry(
                    devicePath,
                    Event.CREATE,
                    fileObserver
                );
                fsWatchRef.addFileEntry(fileWatchEntry);

                long deadline = System.currentTimeMillis() + waitTimeoutAfterCreateMillis;
                try
                {
                    errorReporterRef.logTrace(
                        "Waiting until device [%s] appears (up to %dms)",
                        devicePathStr,
                        waitTimeoutAfterCreateMillis
                    );
                    while(!Files.exists(devicePath) && System.currentTimeMillis() < deadline)
                    {
                        syncObj.wait(Math.max(1, deadline - System.currentTimeMillis()));
                    }
                }
                catch (InterruptedException interruptedExc)
                {
                    throw new StorageException(
                        "Interrupted exception while waiting for device '" + devicePathStr + "' to show up",
                        interruptedExc
                    );
                }
                finally
                {
                    fsWatchRef.removeFileEntry(fileWatchEntry);
                }
                if (!Files.exists(devicePath))
                {
                    throw new StorageException(
                        "Device '" + devicePathStr + "' did not show up in " +
                            waitTimeoutAfterCreateMillis + "ms"
                    );
                }
                errorReporterRef.logTrace(
                    "Device [%s] appeared after %sms",
                    devicePathStr,
                    System.currentTimeMillis() - start
                );
            }
        }
        catch (IOException exc)
        {
            throw new StorageException(
                "Unable to register file watch event for device '" + devicePathStr + "' being created",
                exc
            );
        }
    }
}
