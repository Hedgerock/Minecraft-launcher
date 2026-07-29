package com.launcher.core.operation.impl;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.LoadManifestTask;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class LoadManifestOperation extends LaunchOperation {
    private final ManifestService manifestService;

    public LoadManifestOperation(LaunchContext context, ExecutionStrategy executionStrategy, ManifestService manifestService) {
        super(context, executionStrategy);
        this.manifestService = manifestService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new LoadManifestTask(
                        this.manifestService
                )
        );
    }
}
