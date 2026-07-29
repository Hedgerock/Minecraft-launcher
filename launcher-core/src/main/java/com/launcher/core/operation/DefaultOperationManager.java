package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;

public class DefaultOperationManager implements OperationManager {

    private final OperationFactory operationFactory;
    private final ExecutionStrategy executionStrategy;

    public DefaultOperationManager(OperationFactory operationFactory, ExecutionStrategy executionStrategy) {
        this.operationFactory = operationFactory;
        this.executionStrategy = executionStrategy;
    }

    @Override
    public OperationResult execute(OperationType type, LaunchContext context) {
        LaunchOperation operation = operationFactory.create(
                type,
                context,
                executionStrategy
        );

        return operation.execute();
    }

}
