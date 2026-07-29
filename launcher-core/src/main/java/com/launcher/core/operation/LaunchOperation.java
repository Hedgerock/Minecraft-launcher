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
        OperationResult result;

        try {
            beforeExecute();

            List<LauncherTask> tasks = createTask();
            result = executeTasks(tasks);

            afterExecute(result);
        } catch (Exception e) {
            result = OperationResult.failure(e.getMessage());
        }

        finalizeOperation(result);

        return result;
    }

    protected void beforeExecute() {}

    protected OperationResult executeTasks(List<LauncherTask> tasks) {
        return executionStrategy.execute(tasks, launchContext);
    }

    protected void afterExecute(OperationResult result) {}

    protected void finalizeOperation(OperationResult result) {}

    protected abstract List<LauncherTask> createTask();
}
