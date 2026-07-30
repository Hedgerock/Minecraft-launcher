package com.launcher.core.operation;

import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.OperationCompletedEvent;
import com.launcher.core.event.events.OperationFailedEvent;
import com.launcher.core.event.events.OperationStartedEvent;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public abstract class LaunchOperation {

    protected final LaunchContext launchContext;
    protected final ExecutionStrategy executionStrategy;
    protected final OperationType operationType;
    protected final EventBus eventBus;

    public LaunchOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            OperationType operationType,
            EventBus eventBus
    ) {
        this.launchContext = launchContext;
        this.executionStrategy = executionStrategy;
        this.operationType = operationType;
        this.eventBus = eventBus;
    }

    public final OperationResult execute() {
        OperationResult result;

        eventBus.publish(new OperationStartedEvent(operationType));

        try {
            beforeExecute();

            List<LauncherTask> tasks = createTask();
            result = executeTasks(tasks);

            afterExecute(result);
        } catch (Exception e) {
            result = OperationResult.failure(resolveErrorMessage(e));
        }

        result = finalizeSafety(result);
        publishCompletionEvent(result);

        return result;
    }

    private void publishCompletionEvent(OperationResult result) {
        if (result.isSuccess()) {
            eventBus.publish(new OperationCompletedEvent(operationType));
            return;
        }

        eventBus.publish(new OperationFailedEvent(
                operationType,
                result
                        .errorMessage()
                        .orElse("Unknown operation failure")
        ));
    }

    private String resolveErrorMessage(Exception exception) {
        final boolean isEmptyOrBlank = exception.getMessage() == null || exception.getMessage().isBlank();

        if (isEmptyOrBlank) {
            return exception.getClass().getSimpleName();
        }

        return exception.getMessage();
    }

    protected void beforeExecute() {}

    private OperationResult finalizeSafety(OperationResult result) {

        try {
            finalizeOperation(result);
            return result;
        } catch (Exception e) {
            return OperationResult.failure(resolveErrorMessage(e));
        }

    }

    protected OperationResult executeTasks(List<LauncherTask> tasks) {
        return executionStrategy.execute(tasks, launchContext);
    }

    protected void afterExecute(OperationResult result) {}

    protected void finalizeOperation(OperationResult result) {}

    protected abstract List<LauncherTask> createTask();
}
