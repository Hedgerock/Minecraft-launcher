package com.launcher.core.operation.factory;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;

public interface OperationFactory {

    LaunchOperation create(
            OperationType type,
            LaunchContext context,
            ExecutionStrategy executionStrategy
    );

}
