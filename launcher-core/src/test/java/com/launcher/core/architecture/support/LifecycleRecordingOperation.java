package com.launcher.core.architecture.support;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class LifecycleRecordingOperation extends LaunchOperation {
    public static final String BEFORE_EXECUTE = "beforeExecute";
    public static final String CREATE_TASK = "createTask";
    public static final String AFTER_EXECUTE = "afterExecute";
    public static final String FINALIZE_OPERATION = "finalizeOperation";

    private final RecordingEvents events;

    public LifecycleRecordingOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            OperationType operationType,
            EventBus eventBus,
            RecordingEvents events
    ) {
        super(launchContext, executionStrategy, operationType, eventBus);
        this.events = events;
    }

    @Override
    protected void beforeExecute() {
        events.record(BEFORE_EXECUTE);
    }

    @Override
    protected List<LauncherTask> createTask() {
        events.record(CREATE_TASK);

        return List.of();
    }

    @Override
    protected void afterExecute(OperationResult result) {
        events.record(AFTER_EXECUTE);
    }

    @Override
    protected void finalizeOperation(OperationResult result) {
       events.record(FINALIZE_OPERATION);
    }
}
