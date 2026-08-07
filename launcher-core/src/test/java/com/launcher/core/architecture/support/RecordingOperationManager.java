package com.launcher.core.architecture.support;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;

import java.util.*;
import java.util.function.Consumer;

public final class RecordingOperationManager implements OperationManager {
    private final Map<OperationType, Queue<OperationResult>> operationResults = new HashMap<>();
    private final Map<OperationType, Queue<Consumer<LaunchContext>>> behaviors = new HashMap<>();

    private final List<OperationType> executedOperationTypes = new ArrayList<>();
    private LaunchContext receivedContext;

    @Override
    public OperationResult execute(OperationType type, LaunchContext context) {
        this.executedOperationTypes.add(type);
        this.receivedContext = context;

        Queue<Consumer<LaunchContext>> queue = behaviors.get(type);

        if (queue != null && !queue.isEmpty()) {
            queue.poll().accept(context);
        }

        Queue<OperationResult> resultQueue = operationResults.get(type);
        OperationResult result = OperationResult.success();

        if (resultQueue != null && !resultQueue.isEmpty()) {
            result = resultQueue.poll();
        }

        return result;
    }

    public void registerResult(OperationType type, OperationResult result) {
        operationResults
            .computeIfAbsent(type, key -> new ArrayDeque<>())
            .add(result);
    }

    public void registerBehavior(
            OperationType type,
            Consumer<LaunchContext> behavior
    ) {
        behaviors
            .computeIfAbsent(type, key -> new ArrayDeque<>())
            .add(behavior);
    }

    public List<OperationType> getExecutedOperationTypes() {
        return executedOperationTypes;
    }

    public LaunchContext getReceivedContext() {
        return receivedContext;
    }
}
