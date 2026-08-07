package com.launcher.core.architecture.download;

import com.launcher.core.architecture.support.RecordingDownloadService;
import com.launcher.core.architecture.support.RecordingEventBus;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadFilesTask;
import com.launcher.core.download.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.download.DownloadCompletedEvent;
import com.launcher.core.event.events.download.DownloadProgressChangedEvent;
import com.launcher.core.event.events.download.DownloadStartedEvent;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadFilesTaskTest {
    private long totalBytes(DownloadPlan plan) {
        return plan.files().stream().mapToLong(FileEntry::size).sum();
    }

    @Test
    void should_not_publish_download_events_when_download_plan_is_empty() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        RecordingDownloadService downloadService = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(downloadService, eventBus);

        LaunchContext context = getContext(false);

        //when
        task.execute(context);

        //then
        assertTrue(
                eventBus.eventsOfType(DownloadCompletedEvent.class).isEmpty()
        );

        assertTrue(
                eventBus.eventsOfType(DownloadProgressChangedEvent.class).isEmpty()
        );

        assertTrue(
                eventBus.eventsOfType(DownloadStartedEvent.class).isEmpty()
        );
    }

    @Test
    void should_not_publish_download_completed_event_when_download_failed() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        RecordingDownloadService downloadService = new RecordingDownloadService(true);

        DownloadFilesTask task = new DownloadFilesTask(downloadService, eventBus);

        LaunchContext context = getContext(true);

        //when
        task.execute(context);

        //then
        assertTrue(
                eventBus.eventsOfType(DownloadCompletedEvent.class).isEmpty()
        );

        assertTrue(
                eventBus.eventsOfType(DownloadProgressChangedEvent.class).isEmpty()
        );

        assertEquals(
                1,
                eventBus.eventsOfType(DownloadStartedEvent.class).size()
        );
    }

    @Test
    void should_publish_download_completed_event_when_download_succeeded() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        RecordingDownloadService downloadService = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(downloadService, eventBus);
        DownloadPlan downloadPlan = getDownloadPlan();

        LaunchContext context = getContext(true);

        //when
        task.execute(context);

        //then
        DownloadCompletedEvent event = eventBus.firstEventOfType(DownloadCompletedEvent.class);

        assertEquals(downloadPlan.files().size(), event.totalFiles());
        assertEquals(totalBytes(downloadPlan), event.totalBytes());
    }

    @Test
    void should_publish_download_progress_changed_event_when_download_completed() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        RecordingDownloadService downloadService = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(downloadService, eventBus);
        DownloadPlan downloadPlan = getDownloadPlan();

        LaunchContext context = getContext(true);

        //when
        task.execute(context);

        //then
        DownloadProgressChangedEvent event = eventBus.firstEventOfType(DownloadProgressChangedEvent.class);

        assertEquals(downloadPlan.files().size(), event.downloadedFiles());
        assertEquals(downloadPlan.files().size(), event.totalFiles());

        assertEquals(totalBytes(downloadPlan), event.downloadedBytes());
        assertEquals(totalBytes(downloadPlan), event.totalBytes());
    }

    @Test
    void should_publish_download_started_event() {
       //given
        RecordingEventBus eventBus = new RecordingEventBus();
        RecordingDownloadService downloadService = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(downloadService, eventBus);

        LaunchContext context = getContext(true);
        DownloadPlan downloadPlan = context.getDownloadPlan();

        //when
        task.execute(context);

        //then
        assertEquals(1, eventBus.eventsOfType(DownloadStartedEvent.class).size());

        DownloadStartedEvent event = eventBus.firstEventOfType(DownloadStartedEvent.class);

        assertEquals(downloadPlan.files().size(), event.totalFiles());
        assertEquals(totalBytes(downloadPlan), event.totalBytes());
    }

    @Test
    void should_return_failure_when_download_is_finished_with_exceptions() {
        //given
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext(true);
        RecordingDownloadService service = new RecordingDownloadService(true);

        DownloadFilesTask task = new DownloadFilesTask(service, eventBus);

        //when
        Result result = task.execute(context);

        //then
        assertNotNull(service.getDownloadPlan());
        assertInstanceOf(FailureResult.class, result);
    }

    @Test
    void should_return_success_when_download_plan_is_empty() {
        //given
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext(true, true);
        DownloadPlan expectedPlan = getEmptyDownloadPlan();
        RecordingDownloadService service = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(service, eventBus);

        //when
        Result result = task.execute(context);

        //then
        assertNotNull(context.getDownloadPlan());
        assertEquals(expectedPlan, context.getDownloadPlan());
        assertNull(service.getDownloadPlan());
        assertTrue(result.success());
        assertInstanceOf(SuccessResult.class, result);
    }

    @Test
    void should_return_failure_when_download_plan_is_missing() {
        //given
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext(false);
        RecordingDownloadService service = new RecordingDownloadService();
        DownloadFilesTask task = new DownloadFilesTask(service, eventBus);

        //when
        Result result = task.execute(context);

        //then
        assertNull(service.getDownloadPlan());
        assertFalse(result.success());
        assertInstanceOf(FailureResult.class, result);
    }

    @Test
    void should_download_files_from_download_plan() {
        //given
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext(true);
        DownloadPlan expectedPlan = getDownloadPlan();
        RecordingDownloadService service = new RecordingDownloadService();
        DownloadFilesTask task = new DownloadFilesTask(service, eventBus);

        //when
        Result result = task.execute(context);

        //then
        assertNotNull(service.getDownloadPlan());
        assertEquals(expectedPlan, service.getDownloadPlan());
        assertTrue(result.success());
        assertInstanceOf(SuccessResult.class, result);
    }

    private LaunchContext getContext(boolean withDownloadPlan) {
        return getContext(withDownloadPlan, false);
    }

    private LaunchContext getContext(boolean withDownloadPlan, boolean withEmptyDownloadPlan) {
        LaunchContext launchContext = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("current-path"),
                        Path.of("")
                )
        );

        if (withDownloadPlan) {
            launchContext.setDownloadPlan(
                    withEmptyDownloadPlan
                            ? getEmptyDownloadPlan()
                            : getDownloadPlan()
            );
        }

        return launchContext;
    }
    private DownloadPlan getDownloadPlan() {
        return new DownloadPlan(
                List.of(getFileEntry())
        );
    }

    private DownloadPlan getEmptyDownloadPlan() {
        return new DownloadPlan(List.of());
    }

    private FileEntry getFileEntry() {
        return getFileEntry("current_path.jar");
    }

    @SuppressWarnings("SameParameterValue")
    private FileEntry getFileEntry(String path) {
        return new FileEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test.com/" + path
        );
    }
}
