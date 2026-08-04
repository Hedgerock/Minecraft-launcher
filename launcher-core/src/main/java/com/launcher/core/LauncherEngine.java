package com.launcher.core;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;

public class LauncherEngine {

    private final LauncherStateMachine stateMachine;
    private final OperationManager operationManager;

    public LauncherEngine(LauncherStateMachine stateMachine, OperationManager operationManager) {
        this.stateMachine = stateMachine;
        this.operationManager = operationManager;
    }

    public void launch(LauncherConfiguration configuration) {
        LaunchContext context = new LaunchContext(configuration);

        stateMachine.transition(LauncherState.LOADING_MANIFEST);

        OperationResult loadManifestResult = operationManager.execute(
                OperationType.LOAD_MANIFEST,
                context
        );

        if (!loadManifestResult.isSuccess()) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        stateMachine.transition(LauncherState.VERIFYING_FILES);

        OperationResult verifyFilesResult = operationManager.execute(
          OperationType.VERIFY_FILES,
          context
        );

        if (!verifyFilesResult.isSuccess()) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        boolean isVerificationPlanEmptyOrIsNotValid =
                context.getVerificationPlan() == null ||
                        !context.getVerificationPlan().isValid();

        if (isVerificationPlanEmptyOrIsNotValid) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        stateMachine.transition(LauncherState.RUNNING);

    }
}
