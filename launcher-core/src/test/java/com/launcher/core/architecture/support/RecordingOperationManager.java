package com.launcher.core.architecture.support;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RecordingOperationManager implements OperationManager {
    private final Map<OperationType, OperationResult> operationResults = new HashMap<>();
    private final Map<OperationType, Consumer<LaunchContext>> behaviors = new HashMap<>();

    private final List<OperationType> executedOperationTypes = new ArrayList<>();
    private LaunchContext receivedContext;

    @Override
    public OperationResult execute(OperationType type, LaunchContext context) {
        this.executedOperationTypes.add(type);
        this.receivedContext = context;

        Consumer<LaunchContext> behavior = behaviors.get(type);

        if (behavior != null) {
            behavior.accept(context);
        }

        return operationResults.getOrDefault(
                type,
                OperationResult.success()
        );
    }

    public void registerResult(OperationType type, OperationResult result) {
        operationResults.put(type, result);
    }

    public void registerBehavior(
            OperationType type,
            Consumer<LaunchContext> behavior
    ) {
        behaviors.put(type, behavior);
    }

    public List<OperationType> getExecutedOperationTypes() {
        return executedOperationTypes;
    }

    public LaunchContext getReceivedContext() {
        return receivedContext;
    }
}
