package com.launcher.core.architecture.support;

import com.launcher.core.event.EventBus;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class FailingWithoutMessageOperation extends LaunchOperation {

    public FailingWithoutMessageOperation(
            LaunchContext launchContext,
            EventBus eventBus
    ) {
        super(
                launchContext,
                new FixedResultExecutionStrategy(OperationResult.success()),
                OperationType.REPAIR,
                eventBus
        );
    }

    @Override
    protected List<LauncherTask> createTask() {
        throw new IllegalStateException();
    }
}
