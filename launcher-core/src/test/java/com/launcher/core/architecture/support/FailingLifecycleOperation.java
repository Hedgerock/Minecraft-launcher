package com.launcher.core.architecture.support;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class FailingLifecycleOperation extends LaunchOperation {
    public static final String BEFORE_EXECUTE = "beforeExecute";
    public static final String CREATE_TASK = "createTask";
    public static final String FINALIZE_OPERATION = "finalizeOperation";
    private static final String EXCEPTION_MESSAGE = "create task failed";

    private final RecordingEvents events;

    public FailingLifecycleOperation(LaunchContext launchContext, ExecutionStrategy executionStrategy, RecordingEvents events) {
        super(launchContext, executionStrategy);
        this.events = events;
    }

    @Override
    protected void beforeExecute() {
        events.record(BEFORE_EXECUTE);
    }

    @Override
    protected List<LauncherTask> createTask() {
        events.record(CREATE_TASK);

        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    protected void finalizeOperation(OperationResult result) {
        events.record(FINALIZE_OPERATION);
    }
}
