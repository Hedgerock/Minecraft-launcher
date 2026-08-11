package com.launcher.core.architecture.support;

import com.launcher.core.event.EventBus;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class EventPublishingOperation extends LaunchOperation {

    private final OperationResult result;

    public EventPublishingOperation(
            LaunchContext launchContext,
            EventBus eventBus,
            OperationResult result
    ) {
        super(
                launchContext,
                new FixedResultExecutionStrategy(result),
                OperationType.REPAIR,
                eventBus
        );
        this.result = result;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of();
    }

    public OperationResult execute(List<LauncherTask> tasks, LaunchContext context) {
        return result;
    }
}
