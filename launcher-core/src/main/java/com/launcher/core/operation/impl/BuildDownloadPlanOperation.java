package com.launcher.core.operation.impl;

import com.launcher.core.download.BuildDownloadPlanTask;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class BuildDownloadPlanOperation extends LaunchOperation {
    private final DownloadPlanBuilder downloadPlanBuilder;

    public BuildDownloadPlanOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            DownloadPlanBuilder downloadPlanBuilder
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.BUILD_DOWNLOAD_PLAN,
                eventBus
        );
        this.downloadPlanBuilder = downloadPlanBuilder;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new BuildDownloadPlanTask(downloadPlanBuilder)
        );
    }
}
