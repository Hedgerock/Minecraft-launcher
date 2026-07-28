package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public abstract class LaunchOperation {

    protected final LaunchContext context;
    protected final ExecutionStrategy executionStrategy;

    public LaunchOperation(LaunchContext context, ExecutionStrategy executionStrategy) {
        this.context = context;
        this.executionStrategy = executionStrategy;
    }

    public OperationResult execute() {
        List<LauncherTask> tasks = createTask();

        return executionStrategy.execute(tasks);
    }

    protected abstract List<LauncherTask> createTask();
}
