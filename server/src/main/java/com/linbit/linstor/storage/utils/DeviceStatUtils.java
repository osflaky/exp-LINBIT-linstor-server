package com.linbit.linstor.storage.utils;

import com.linbit.ChildProcessTimeoutException;
import com.linbit.extproc.ExtCmd.OutputData;
import com.linbit.extproc.ExtCmdFactory;
import com.linbit.linstor.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Resolves LINSTOR block-device paths (LVM LVs, ZFS zvols, whole disks, ...) to their sysfs 'stat'
 * file, so the I/O-progress watchdog in {@link com.linbit.extproc.ChildProcessHandler} can monitor
 * block-layer activity on those devices in addition to {@code /proc/<pid>/io}.
 *
 * <p>Lives in the server module (rather than next to the satellite storage utils) so that any
 * component with an {@link ExtCmdFactory} - satellite <em>or</em> controller - can opt an external
 * command into device-aware I/O progress monitoring.</p>
 *
 * <p>Resolution strategy (cheap path first):</p>
 * <ol>
 *   <li>{@link Path#toRealPath} and, if the result has the shape {@code /dev/<single-segment>} (e.g.
 *       {@code /dev/dm-5}, {@code /dev/zd16}, {@code /dev/sdb}), use {@code /sys/block/<name>/stat}.
 *       This covers LINSTOR's device types with zero subprocesses.</li>
 *   <li>Otherwise fall back to resolving the device's {@code major:minor} via
 *       {@code lsblk --nodeps -n -o MAJ:MIN <dev>} and use {@code /sys/dev/block/<maj>:<min>/stat}
 *       ({@code --nodeps} lists only the device itself, so children/holders can never shadow it).</li>
 *   <li>If neither works, the path is dropped (and logged) so the caller gracefully degrades to
 *       {@code /proc/<pid>/io}-only monitoring for that device.</li>
 * </ol>
 *
 * <p>Intended to be called once, at command setup time, on the device paths collected by
 * {@code LayerVlmUtils#getStorageDevicePaths(...)}; the resulting set is then passed to
 * {@link com.linbit.extproc.ChildProcessHandler#setIoProgressMode(boolean, Collection)}.</p>
 */
public class DeviceStatUtils
{
    private static final String DEV_DIR = "/dev";

    private DeviceStatUtils()
    {
    }

    /**
     * Resolves each device path to its sysfs 'stat' file, silently dropping any that cannot be
     * resolved. Never throws: resolution failures degrade to fewer monitored devices, never to a
     * failed command.
     */
    public static Set<String> resolveSysStatFiles(ExtCmdFactory extCmdFactory, Collection<String> devicePathsRef)
    {
        Set<String> statFiles = new TreeSet<>();
        for (String devicePath : devicePathsRef)
        {
            @Nullable String statFile = resolveSysStatFile(extCmdFactory, devicePath);
            if (statFile != null)
            {
                statFiles.add(statFile);
            }
        }
        return statFiles;
    }

    public static @Nullable String resolveSysStatFile(ExtCmdFactory extCmdFactory, String devicePathRef)
    {
        @Nullable String statFile = resolveViaRealPath(devicePathRef);
        if (statFile == null)
        {
            statFile = resolveViaMajorMinor(extCmdFactory, devicePathRef);
        }
        if (statFile == null)
        {
            extCmdFactory.getErrorReporter().logWarning(
                "I/O progress mode: could not resolve a sysfs 'stat' file for device '%s'; " +
                    "falling back to /proc/<pid>/io-only monitoring for this device.",
                devicePathRef
            );
        }
        return statFile;
    }

    private static @Nullable String resolveViaRealPath(String devicePathRef)
    {
        @Nullable String ret = null;
        try
        {
            Path realPath = Paths.get(devicePathRef).toRealPath();
            @Nullable Path parent = realPath.getParent();
            // expect exactly /dev/<single-segment>, i.e. two parts with the first being /dev
            if (parent != null && parent.toString().equals(DEV_DIR))
            {
                String kernelName = realPath.getFileName().toString();
                Path statPath = Paths.get(String.format("/sys/block/%s/stat", kernelName));
                if (Files.isReadable(statPath))
                {
                    ret = statPath.toString();
                }
            }
        }
        catch (IOException ignored)
        {
            // fall through to major:minor resolution
        }
        return ret;
    }

    private static @Nullable String resolveViaMajorMinor(ExtCmdFactory extCmdFactory, String devicePathRef)
    {
        @Nullable String ret = null;
        try
        {
            // --nodeps -> only the device itself (no holders/children); -n -> no header; single "maj:min"
            OutputData outData = extCmdFactory.create()
                .exec("lsblk", "--nodeps", "-n", "-o", "MAJ:MIN", devicePathRef);
            if (outData.exitCode == 0)
            {
                String output = new String(outData.stdoutData, StandardCharsets.UTF_8).trim();
                int newlineIdx = output.indexOf('\n');
                String majMin = (newlineIdx < 0 ? output : output.substring(0, newlineIdx)).trim();
                if (majMin.matches("\\d+:\\d+"))
                {
                    Path statPath = Paths.get(String.format("/sys/dev/block/%s/stat", majMin));
                    if (Files.isReadable(statPath))
                    {
                        ret = statPath.toString();
                    }
                }
            }
        }
        catch (ChildProcessTimeoutException | IOException exc)
        {
            extCmdFactory.getErrorReporter().logWarning(
                "I/O progress mode: lsblk failed while resolving device '%s': %s",
                devicePathRef,
                exc.getMessage()
            );
        }
        return ret;
    }
}
