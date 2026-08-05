package com.launcher.core.operation.impl;

import com.launcher.core.download.DownloadFilesTask;
import com.launcher.core.download.DownloadService;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class DownloadFilesOperation extends LaunchOperation {
    private final DownloadService downloadService;

    public DownloadFilesOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            DownloadService downloadService
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.DOWNLOAD_FILES,
                eventBus
        );

        this.downloadService = downloadService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new DownloadFilesTask(downloadService)
        );
    }
}
