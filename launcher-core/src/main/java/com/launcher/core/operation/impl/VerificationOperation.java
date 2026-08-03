package com.launcher.core.operation.impl;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.VerifyFilesTask;

import java.util.List;

public final class VerificationOperation extends LaunchOperation {
    private final VerificationService verificationService;

    public VerificationOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            VerificationService verificationService
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.VERIFY_FILES,
                eventBus
        );

        this.verificationService = verificationService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new VerifyFilesTask(verificationService)
        );
    }
}
