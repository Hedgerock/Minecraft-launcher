package com.launcher.core.operation.impl;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.task.LauncherTask;

import java.util.Collections;
import java.util.List;

public final class RepairOperation extends LaunchOperation {

    public RepairOperation(LaunchContext context, ExecutionStrategy executionStrategy) {
        super(context, executionStrategy);
    }

    @Override
    protected List<LauncherTask> createTask() {
        return Collections.emptyList();
    }
}
