package com.launcher.core.download;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;

public class DownloadFilesTask implements LauncherTask {
    private final DownloadService downloadService;

    public DownloadFilesTask(DownloadService downloadService) {
        this.downloadService = downloadService;
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

        try {
            downloadService.download(downloadPlan);
        } catch (Exception e) {
            return TaskResult.failure("Failed to download files: " + e.getMessage());
        }

        return TaskResult.success();
    }
}
