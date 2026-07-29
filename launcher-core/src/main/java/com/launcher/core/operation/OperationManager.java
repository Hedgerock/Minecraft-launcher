package com.launcher.core.operation;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;

public interface OperationManager {

    OperationResult execute(
            OperationType type,
            LaunchContext context
    );

}
