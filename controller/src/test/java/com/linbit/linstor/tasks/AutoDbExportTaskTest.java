package com.linbit.linstor.tasks;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.LinStorScope;
import com.linbit.linstor.core.apicallhandler.controller.CtrlPropsHelper;
import com.linbit.linstor.core.apicallhandler.controller.db.DbExportImportHelper;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.propscon.ReadOnlyProps;
import com.linbit.linstor.tasks.TaskScheduleService.Task;
import com.linbit.linstor.tasks.utils.CronUtils;
import com.linbit.linstor.testutils.EmptyErrorReporter;
import com.linbit.utils.TimeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AutoDbExportTask} against a temporary export directory with mocked properties, scheduler and
 * database export.
 */
public class AutoDbExportTaskTest
{
    /**
     * "Every year on January 1st, 00:00". A coarse cron keeps the tests deterministic: the last and next execution
     * points do not change while a test is running.
     */
    private static final String YEARLY_CRON = "0 0 1 1 *";
    private static final String DAILY_CRON = "0 4 * * *";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private Path exportDir;
    private ReadOnlyProps ctrlProps;
    private TaskScheduleService taskScheduleService;
    private DbExportImportHelper dbExportImportHelper;
    private AutoDbExportTask task;

    @Before
    public void setUp() throws Exception
    {
        exportDir = tmpFolder.getRoot().toPath();

        ctrlProps = Mockito.mock(ReadOnlyProps.class);
        CtrlPropsHelper ctrlPropsHelper = Mockito.mock(CtrlPropsHelper.class);
        when(ctrlPropsHelper.getCtrlPropsForView()).thenReturn(ctrlProps);

        taskScheduleService = Mockito.mock(TaskScheduleService.class);

        // the "export" simply creates the target file so that the cleanup logic sees it
        dbExportImportHelper = Mockito.mock(DbExportImportHelper.class);
        doAnswer(invocation ->
        {
            Files.createFile(invocation.getArgument(0));
            return null;
        }).when(dbExportImportHelper).exportTo(any());

        task = new AutoDbExportTask(
            new EmptyErrorReporter(),
            ctrlPropsHelper,
            taskScheduleService,
            new LinStorScope(),
            dbExportImportHelper
        );
    }

    private void setProps(@Nullable String cron, @Nullable String keep, @Nullable Path path, @Nullable Boolean compress)
        throws Exception
    {
        when(
            ctrlProps.getPropWithDefault(
                eq(ApiConsts.KEY_AUTO_DB_EXPORT_CRON),
                eq(ApiConsts.NAMESPC_AUTO_DB_EXPORT),
                anyString()
            )
        ).thenAnswer(invocation -> cron == null ? invocation.getArgument(2) : cron);
        when(
            ctrlProps.getPropWithDefault(
                eq(ApiConsts.KEY_AUTO_DB_EXPORT_COMPRESS),
                eq(ApiConsts.NAMESPC_AUTO_DB_EXPORT),
                anyString()
            )
        ).thenAnswer(inv -> compress == null ? inv.getArgument(2) : compress.toString());
        when(ctrlProps.getProp(ApiConsts.KEY_AUTO_DB_EXPORT_KEEP, ApiConsts.NAMESPC_AUTO_DB_EXPORT))
            .thenReturn(keep);
        when(ctrlProps.getProp(ApiConsts.KEY_AUTO_DB_EXPORT_PATH, ApiConsts.NAMESPC_AUTO_DB_EXPORT))
            .thenReturn(path == null ? null : path.toString());
    }

    private static ZonedDateTime lastExec(String cron)
    {
        return CronUtils.lastExec(CronUtils.asCron(cron), ZonedDateTime.now(ZoneId.systemDefault()));
    }

    private static long nextExecMillis(String cron)
    {
        return CronUtils.nextExec(CronUtils.asCron(cron)).toEpochSecond() * 1000;
    }

    private Path createExportFile(LocalDateTime timestamp) throws Exception
    {
        return Files.createFile(
            exportDir.resolve("auto_db_export_" + TimeUtils.DTF_NO_SPACE.format(timestamp) + ".json.gz")
        );
    }

    private List<Path> listExportFiles() throws Exception
    {
        try (Stream<Path> stream = Files.list(exportDir))
        {
            return stream.filter(path -> path.getFileName().toString().startsWith("auto_db_export_"))
                .sorted()
                .toList();
        }
    }

    @Test
    public void initializeDoesNotTouchScheduler() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);

        task.initialize();

        // the scheduler decides via firstRunAt() when to run the task; a reschedule from initialize would lead to a
        // duplicated entry and therefore to two exports per execution point
        verifyNoInteractions(taskScheduleService);
        verify(dbExportImportHelper, never()).exportTo(any());
    }

    @Test
    public void firstRunAtIsEndTaskWhenCronIsOff() throws Exception
    {
        setProps("off", null, exportDir, null);

        task.initialize();

        assertThat(task.firstRunAt()).isEqualTo(Task.END_TASK);
        verify(taskScheduleService).cancel(task);
    }

    @Test
    public void firstRunAtIsAsapWhenNoExportExists() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        assertThat(task.firstRunAt()).isEqualTo(Task.RUN_ASAP);
    }

    @Test
    public void firstRunAtIsAsapWhenOnlyOlderExportsExist() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();
        createExportFile(lastExec(YEARLY_CRON).toLocalDateTime().minusDays(1));

        assertThat(task.firstRunAt()).isEqualTo(Task.RUN_ASAP);
    }

    @Test
    public void firstRunAtIsNextExecWhenExportExactlyAtLastExecExists() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();
        // a export created by a previous controller instance exactly at the execution point (seconds == 00)
        createExportFile(lastExec(YEARLY_CRON).toLocalDateTime());

        assertThat(task.firstRunAt()).isEqualTo(nextExecMillis(YEARLY_CRON));
    }

    @Test
    public void firstRunAtIsNextExecWhenExportAfterLastExecExists() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();
        createExportFile(lastExec(YEARLY_CRON).toLocalDateTime().plusSeconds(13));

        assertThat(task.firstRunAt()).isEqualTo(nextExecMillis(YEARLY_CRON));
    }

    @Test
    public void firstRunAtIgnoresFilesNotMatchingTheExportPattern() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();
        // one-digit fields do not match the pattern and must not cause a parse exception
        Files.createFile(exportDir.resolve("auto_db_export_2026-9-2_7-20-00.json.gz"));
        Files.createFile(exportDir.resolve("auto_db_export_garbage.json.gz"));
        Files.createFile(exportDir.resolve("linstor_db_export.json"));

        assertThat(task.firstRunAt()).isEqualTo(Task.RUN_ASAP);
    }

    @Test
    public void runExportsIntoExportDirAndReturnsNextExec() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        long ret = task.run(System.currentTimeMillis());

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(dbExportImportHelper).exportTo(pathCaptor.capture());
        Path exportPath = pathCaptor.getValue();
        assertThat(exportPath.getParent()).isEqualTo(exportDir);
        assertThat(exportPath.getFileName().toString())
            .matches("auto_db_export_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}\\.json\\.gz");
        assertThat(exportPath).exists();
        assertThat(ret).isEqualTo(nextExecMillis(YEARLY_CRON));

        // the freshly created export now covers the last execution point
        assertThat(task.firstRunAt()).isEqualTo(nextExecMillis(YEARLY_CRON));
    }

    @Test
    public void runCompressedExports() throws Exception
    {
        compressTest(true);
    }

    @Test
    public void runUncompressedExports() throws Exception
    {
        compressTest(false);
    }

    private void compressTest(boolean compressRef) throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, compressRef);
        task.initialize();

        long ret = task.run(System.currentTimeMillis());

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(dbExportImportHelper).exportTo(pathCaptor.capture());
        Path exportPath = pathCaptor.getValue();
        assertThat(exportPath.getParent()).isEqualTo(exportDir);
        assertThat(exportPath.getFileName().toString())
            .matches(
                "auto_db_export_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}\\.json" +
                    (compressRef ? "\\.gz" : "")
            );
        assertThat(exportPath).exists();
        assertThat(ret).isEqualTo(nextExecMillis(YEARLY_CRON));

        // the freshly created export now covers the last execution point
        assertThat(task.firstRunAt()).isEqualTo(nextExecMillis(YEARLY_CRON));
    }

    @Test
    public void runDeletesOldestExportsExceedingKeep() throws Exception
    {
        setProps(YEARLY_CRON, "3", exportDir, null);
        task.initialize();

        LocalDateTime base = LocalDateTime.of(2020, 1, 1, 4, 0, 0);
        Path oldest = createExportFile(base);
        Path old2 = createExportFile(base.plusDays(1));
        Path old3 = createExportFile(base.plusDays(2));
        Path old4 = createExportFile(base.plusDays(3));
        Path old5 = createExportFile(base.plusDays(4));
        Path unrelated = Files.createFile(exportDir.resolve("linstor_db_export.json"));

        task.run(System.currentTimeMillis());

        // 5 old + 1 new export, keep 3 -> the two newest old ones and the new one survive
        assertThat(oldest).doesNotExist();
        assertThat(old2).doesNotExist();
        assertThat(old3).doesNotExist();
        assertThat(old4).exists();
        assertThat(old5).exists();
        assertThat(unrelated).exists();
        assertThat(listExportFiles()).hasSize(3);
    }

    @Test
    public void runReturnsEndTaskWhenDeactivatedMeanwhile() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        setProps("off", null, exportDir, null);
        task.updateProps(new ApiCallRcImpl());

        assertThat(task.run(System.currentTimeMillis())).isEqualTo(Task.END_TASK);
        verify(taskScheduleService).cancel(task);
    }

    @Test
    public void updatePropsWithUnchangedPropsDoesNothing() throws Exception
    {
        setProps(YEARLY_CRON, "5", exportDir, null);
        task.initialize();

        ApiCallRc apiCallRc = new ApiCallRcImpl();
        task.updateProps(apiCallRc);

        assertThat(apiCallRc).isEmpty();
        verifyNoInteractions(taskScheduleService);
    }

    @Test
    public void updatePropsWithChangedCronReschedulesAtNextExec() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        setProps(DAILY_CRON, null, exportDir, null);
        ApiCallRc apiCallRc = new ApiCallRcImpl();
        long expectedBefore = nextExecMillis(DAILY_CRON);
        task.updateProps(apiCallRc);
        long expectedAfter = nextExecMillis(DAILY_CRON);

        ArgumentCaptor<Long> tsCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskScheduleService).rescheduleAt(eq(task), tsCaptor.capture());
        assertThat(tsCaptor.getValue()).isBetween(expectedBefore, expectedAfter);
        verify(taskScheduleService, never()).cancel(any());

        assertThat(apiCallRc).hasSize(1);
        assertThat(apiCallRc.get(0).getReturnCode()).isEqualTo(ApiConsts.MODIFIED);
        assertThat(apiCallRc.get(0).getMessage()).contains(DAILY_CRON);

        // subsequent runs follow the new cron
        assertThat(task.run(System.currentTimeMillis())).isBetween(expectedBefore, expectedAfter);
    }

    @Test
    public void updatePropsWithNewPathExportsIntoNewDirectory() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        Path newDir = tmpFolder.newFolder("new-exports").toPath();
        setProps(YEARLY_CRON, null, newDir, null);
        ApiCallRc apiCallRc = new ApiCallRcImpl();
        task.updateProps(apiCallRc);

        task.run(System.currentTimeMillis());

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(dbExportImportHelper).exportTo(pathCaptor.capture());
        assertThat(pathCaptor.getValue().getParent()).isEqualTo(newDir);
        assertThat(apiCallRc).hasSize(1);
        assertThat(apiCallRc.get(0).getMessage()).contains(newDir.toString());
    }

    @Test
    public void updatePropsWithMissingPathThrowsAndKeepsOldSettings() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        Path missing = exportDir.resolve("does-not-exist");
        setProps(DAILY_CRON, "1", missing, null);

        assertThatThrownBy(() -> task.updateProps(new ApiCallRcImpl()))
            .isInstanceOf(ApiRcException.class)
            .satisfies(
                exc -> assertThat(((ApiRcException) exc).getApiCallRc().get(0).getReturnCode())
                    .isEqualTo(ApiConsts.FAIL_INVLD_DB_EXPORT_FILE)
            );

        // nothing was applied: no reschedule, old directory and old cron are still in use
        verify(taskScheduleService, never()).rescheduleAt(any(), anyLong());
        long ret = task.run(System.currentTimeMillis());
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(dbExportImportHelper).exportTo(pathCaptor.capture());
        assertThat(pathCaptor.getValue().getParent()).isEqualTo(exportDir);
        assertThat(ret).isEqualTo(nextExecMillis(YEARLY_CRON));
    }

    @Test
    public void updatePropsWithFileAsPathThrows() throws Exception
    {
        setProps(YEARLY_CRON, null, exportDir, null);
        task.initialize();

        Path file = tmpFolder.newFile("not-a-directory").toPath();
        setProps(YEARLY_CRON, null, file, null);

        assertThatThrownBy(() -> task.updateProps(new ApiCallRcImpl()))
            .isInstanceOf(ApiRcException.class)
            .hasMessageContaining("not a directory");
        verifyNoInteractions(taskScheduleService);
    }
}
