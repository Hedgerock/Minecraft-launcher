package com.launcher.core.execution;

import com.launcher.core.operation.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class SequentialExecutionStrategy implements ExecutionStrategy {

    @Override
    public OperationResult execute(List<LauncherTask> tasks) {
        return OperationResult.success();
    }
}
