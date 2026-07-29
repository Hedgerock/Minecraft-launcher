package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public abstract class LaunchOperation {

    protected final LaunchContext launchContext;
    protected final ExecutionStrategy executionStrategy;

    public LaunchOperation(LaunchContext launchContext, ExecutionStrategy executionStrategy) {
        this.launchContext = launchContext;
        this.executionStrategy = executionStrategy;
    }

    public OperationResult execute() {
        List<LauncherTask> tasks = createTask();

        return executionStrategy.execute(tasks, launchContext);
    }

    protected abstract List<LauncherTask> createTask();
}
