package com.launcher.core.execution;

import com.launcher.core.operation.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public interface ExecutionStrategy {

    OperationResult execute(List<LauncherTask> tasks);

}
