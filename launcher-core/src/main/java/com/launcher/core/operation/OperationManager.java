package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;

public class OperationManager {

    private final OperationFactory operationFactory;
    private final ExecutionStrategy executionStrategy;
    private final LaunchContext launchContext;

    public OperationManager(OperationFactory operationFactory, ExecutionStrategy executionStrategy, LaunchContext launchContext) {
        this.operationFactory = operationFactory;
        this.executionStrategy = executionStrategy;
        this.launchContext = launchContext;
    }

    public OperationResult execute(OperationType type) {
        LaunchOperation operation = operationFactory.create(
                type,
                launchContext,
                executionStrategy
        );

        return operation.execute();
    }

}
