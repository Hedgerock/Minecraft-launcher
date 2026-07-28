package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;

public class DefaultOperationFactory implements OperationFactory {

    @Override
    public LaunchOperation create(OperationType type, LaunchContext context, ExecutionStrategy executionStrategy) {
        return switch (type) {
            case REPAIR -> new RepairOperation(
                    context,
                    executionStrategy
            );
        };
    }
}
