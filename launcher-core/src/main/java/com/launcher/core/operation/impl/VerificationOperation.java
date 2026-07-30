package com.launcher.core.operation.impl;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class VerificationOperation extends LaunchOperation {

    public VerificationOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.VERIFY_FILES,
                eventBus
        );
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of();
    }
}
