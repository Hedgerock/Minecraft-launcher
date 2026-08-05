package com.launcher.core;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.VerificationPlan;

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

        VerificationPlan verificationPlan = context.getVerificationPlan();

        if (verificationPlan == null) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        if (verificationPlan.isValid()) {
            stateMachine.transition(LauncherState.RUNNING);
            return;
        }

        OperationResult buildDownloadPlanResult = operationManager.execute(
                OperationType.BUILD_DOWNLOAD_PLAN,
                context
        );

        if (!buildDownloadPlanResult.isSuccess()) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        stateMachine.transition(LauncherState.FAILED);

    }
}
