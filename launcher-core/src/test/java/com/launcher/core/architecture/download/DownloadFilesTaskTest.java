package com.launcher.core.architecture.download;

import com.launcher.core.architecture.support.RecordingDownloadService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadFilesTask;
import com.launcher.core.download.DownloadPlan;
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

    @Test
    void should_return_failure_when_download_is_finished_with_exceptions() {
        //given
        LaunchContext context = getContext(true);
        RecordingDownloadService service = new RecordingDownloadService(true);

        DownloadFilesTask task = new DownloadFilesTask(service);

        //when
        Result result = task.execute(context);

        //then
        assertNotNull(service.getDownloadPlan());
        assertInstanceOf(FailureResult.class, result);
    }

    @Test
    void should_return_success_when_download_plan_is_empty() {
        //given
        LaunchContext context = getContext(true, true);
        DownloadPlan expectedPlan = getEmptyDownloadPlan();
        RecordingDownloadService service = new RecordingDownloadService();

        DownloadFilesTask task = new DownloadFilesTask(service);

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
        LaunchContext context = getContext(false);
        RecordingDownloadService service = new RecordingDownloadService();
        DownloadFilesTask task = new DownloadFilesTask(service);

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
        LaunchContext context = getContext(true);
        DownloadPlan expectedPlan = getDownloadPlan();
        RecordingDownloadService service = new RecordingDownloadService();
        DownloadFilesTask task = new DownloadFilesTask(service);

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

    private FileEntry getFileEntry(String path) {
        return new FileEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test.com/" + path
        );
    }
}
