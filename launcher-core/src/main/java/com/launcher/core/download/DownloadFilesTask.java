package com.launcher.core.download;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.download.DownloadCompletedEvent;
import com.launcher.core.event.events.download.DownloadProgressChangedEvent;
import com.launcher.core.event.events.download.DownloadStartedEvent;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.model.manifest.ResourceEntry;

import java.util.List;

public class DownloadFilesTask implements LauncherTask {
    private final DownloadService downloadService;
    private final EventBus eventBus;

    public DownloadFilesTask(DownloadService downloadService, EventBus eventBus) {
        this.downloadService = downloadService;
        this.eventBus = eventBus;
    }

    @Override
    public LauncherState state() {
        return LauncherState.DOWNLOADING;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        DownloadPlan downloadPlan = launchContext.getDownloadPlan();

        if (downloadPlan == null) {
            return TaskResult.failure("Download plan not built");
        }

        if (downloadPlan.isEmpty()) {
            return TaskResult.success();
        }

        DownloadStartedEvent downloadStartedEvent = publishDownloadStartedEvent(downloadPlan);

        try {
            downloadService.download(downloadPlan);
            publishDownloadProgressChangedEvent(downloadStartedEvent);
        } catch (Exception e) {
            return TaskResult.failure("Failed to download files: " + e.getMessage());
        }

        publishDownloadCompletedEvent(downloadStartedEvent);
        return TaskResult.success();
    }

    private void publishDownloadCompletedEvent(DownloadStartedEvent event) {
        eventBus.publish(
                new DownloadCompletedEvent(
                        event.totalFiles(),
                        event.totalBytes()
                )
        );
    }

    private void publishDownloadProgressChangedEvent(DownloadStartedEvent event) {
        eventBus.publish(
                new DownloadProgressChangedEvent(
                        event.totalFiles(),
                        event.totalFiles(),
                        event.totalBytes(),
                        event.totalBytes()
                )
        );
    }

    private DownloadStartedEvent publishDownloadStartedEvent(DownloadPlan downloadPlan) {
        List<ResourceEntry> files = downloadPlan.resources();

        int totalFiles = files.size();
        long totalBytes = files.stream()
            .mapToLong(ResourceEntry::size)
            .sum();

        DownloadStartedEvent downloadStartedEvent = new DownloadStartedEvent(totalFiles, totalBytes);

        eventBus.publish(downloadStartedEvent);

        return downloadStartedEvent;
    }
}
