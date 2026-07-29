package com.launcher.core.execution;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class SequentialExecutionStrategy implements ExecutionStrategy {

    @Override
    public OperationResult execute(
            List<LauncherTask> tasks,
            LaunchContext launchContext
    ) {

        for (LauncherTask task: tasks) {
            Result result = task.execute(launchContext);

            if (!result.success()) {

                FailureResult failureResult = (FailureResult) result;

                return OperationResult.failure(failureResult.getMessage());
            }
        }

        return OperationResult.success();
    }
}
