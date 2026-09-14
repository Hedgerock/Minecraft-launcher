package com.launcher.core;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.VerificationPlan;

public final class LauncherEngine {

    private final LauncherStateMachine stateMachine;
    private final OperationManager operationManager;

    public LauncherEngine(LauncherStateMachine stateMachine, OperationManager operationManager) {
        this.stateMachine = stateMachine;
        this.operationManager = operationManager;
    }


    public LaunchResult launch(LauncherConfiguration configuration) {
        LaunchContext context = new LaunchContext(configuration);

        if (operationFailed(
                LauncherState.LOADING_MANIFEST,
                OperationType.LOAD_MANIFEST,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }


        if (operationFailed(
                LauncherState.VERIFYING_FILES,
                OperationType.VERIFY_FILES,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }


        VerificationPlan verificationPlan = getVerificationPlanOrFail(context);

        if (verificationPlan == null) {
            return LaunchResult.failure(LauncherState.FAILED);
        }

        if (!verificationPlan.isValid()) {

            if (operationFailed(
                    LauncherState.BUILDING_DOWNLOAD_PLAN,
                    OperationType.BUILD_DOWNLOAD_PLAN,
                    context
            )) {
                return LaunchResult.failure(LauncherState.FAILED);
            }

            if (operationFailed(
                    LauncherState.DOWNLOADING,
                    OperationType.DOWNLOAD_FILES,
                    context
            )) {
                return LaunchResult.failure(LauncherState.FAILED);
            }

            if (operationFailed(
                    LauncherState.VERIFYING_FILES,
                    OperationType.VERIFY_FILES,
                    context
            )) {
                return LaunchResult.failure(LauncherState.FAILED);
            }

            VerificationPlan downloadedVerificationPlan = getVerificationPlanOrFail(context);

            if (downloadedVerificationPlan == null) {
                return LaunchResult.failure(LauncherState.FAILED);
            }

            if (!downloadedVerificationPlan.isValid()) {
                stateMachine.transition(LauncherState.FAILED);
                return LaunchResult.failure(LauncherState.FAILED);
            }
        }

        if (operationFailed(
                LauncherState.PREPARING_GAME,
                OperationType.PREPARE_DIRECTORIES,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }

        if (operationFailed(
                LauncherState.EXTRACTING_NATIVES,
                OperationType.EXTRACT_NATIVES,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }

        if (operationFailed(
                LauncherState.BUILDING_GAME_LAUNCH_PLAN,
                OperationType.BUILD_GAME_LAUNCH_PLAN,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }

        if (operationFailed(
                LauncherState.LAUNCHING,
                OperationType.LAUNCH_GAME,
                context
        )) {
            return LaunchResult.failure(LauncherState.FAILED);
        }

        stateMachine.transition(LauncherState.RUNNING);

        return LaunchResult.success(LauncherState.RUNNING);
    }

    private boolean operationFailed(
            LauncherState state,
            OperationType type,
            LaunchContext context
    ) {

        stateMachine.transition(state);

        OperationResult result = operationManager.execute(type, context);

        if (!result.isSuccess()) {
            stateMachine.transition(LauncherState.FAILED);
            return true;
        }

        return false;
    }

    private VerificationPlan getVerificationPlanOrFail(LaunchContext context) {
        VerificationPlan plan = context.getVerificationPlan();

        if (plan == null) {
            stateMachine.transition(LauncherState.FAILED);
        }

        return plan;
    }
}
