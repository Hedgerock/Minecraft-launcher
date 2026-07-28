package com.launcher.core.operation;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;

public interface OperationFactory {

    LaunchOperation create(
            OperationType type,
            LaunchContext context,
            ExecutionStrategy executionStrategy
    );

}
