package com.launcher.core.architecture.support;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class FixedResultExecutionStrategy implements ExecutionStrategy {

    private final OperationResult result;

    public FixedResultExecutionStrategy(OperationResult result) {
        this.result = result;
    }

    @Override
    public OperationResult execute(List<LauncherTask> tasks, LaunchContext context) {
        return result;
    }
}
