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

    public LauncherEngine(
            LauncherStateMachine stateMachine,
            OperationManager operationManager
    ) {
        this.stateMachine = stateMachine;
        this.operationManager = operationManager;
    }


    public LaunchResult launch(LauncherConfiguration configuration) {
        LaunchContext context = new LaunchContext(configuration);

        OperationResult loadManifestResult = executeOperation(
                LauncherState.LOADING_MANIFEST,
                OperationType.LOAD_MANIFEST,
                context
        );

        if (!loadManifestResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.LOAD_MANIFEST,
                    loadManifestResult
            );
        }

        OperationResult verifyFilesResult = executeOperation(
                LauncherState.VERIFYING_FILES,
                OperationType.VERIFY_FILES,
                context
        );

        if (!verifyFilesResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.VERIFY_FILES,
                    verifyFilesResult
            );
        }

        VerificationPlan verificationPlan = context.getVerificationPlan();

        if (verificationPlan == null) {
            return lifecycleFailureResult(
                    "Verification plan is missing"
            );
        }

        if (!verificationPlan.isValid()) {

            OperationResult buildingDownloadPlanResult = executeOperation(
                    LauncherState.BUILDING_DOWNLOAD_PLAN,
                    OperationType.BUILD_DOWNLOAD_PLAN,
                    context
            );

            if (!buildingDownloadPlanResult.isSuccess()) {
                return operationFailureResult(
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        buildingDownloadPlanResult
                );
            }

            OperationResult downloadFilesResult = executeOperation(
                    LauncherState.DOWNLOADING,
                    OperationType.DOWNLOAD_FILES,
                    context
            );

            if (!downloadFilesResult.isSuccess()) {
                return operationFailureResult(
                        OperationType.DOWNLOAD_FILES,
                        downloadFilesResult
                );
            }

            OperationResult reverifyFilesResult = executeOperation(
                    LauncherState.VERIFYING_FILES,
                    OperationType.VERIFY_FILES,
                    context
            );

            if (!reverifyFilesResult.isSuccess()) {
                return operationFailureResult(
                        OperationType.VERIFY_FILES,
                        reverifyFilesResult
                );
            }

            VerificationPlan downloadedVerificationPlan = context.getVerificationPlan();

            if (downloadedVerificationPlan == null) {
                return lifecycleFailureResult("Post-download verification plan is missing");
            }

            if (!downloadedVerificationPlan.isValid()) {
                return lifecycleFailureResult("Post-download verification plan is invalid");
            }
        }

        OperationResult prepareDirectoriesResult = executeOperation(
                LauncherState.PREPARING_GAME,
                OperationType.PREPARE_DIRECTORIES,
                context
        );

        if (!prepareDirectoriesResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.PREPARE_DIRECTORIES,
                    prepareDirectoriesResult
            );
        }

        OperationResult extractNativesResult = executeOperation(
                LauncherState.EXTRACTING_NATIVES,
                OperationType.EXTRACT_NATIVES,
                context
        );

        if (!extractNativesResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.EXTRACT_NATIVES,
                    extractNativesResult
            );
        }

        OperationResult buildGameLaunchPlanResult = executeOperation(
                LauncherState.BUILDING_GAME_LAUNCH_PLAN,
                OperationType.BUILD_GAME_LAUNCH_PLAN,
                context
        );

        if (!buildGameLaunchPlanResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.BUILD_GAME_LAUNCH_PLAN,
                    buildGameLaunchPlanResult
            );
        }

        OperationResult launchGameResult = executeOperation(
                LauncherState.LAUNCHING,
                OperationType.LAUNCH_GAME,
                context
        );

        if (!launchGameResult.isSuccess()) {
            return operationFailureResult(
                    OperationType.LAUNCH_GAME,
                    launchGameResult
            );
        }

        stateMachine.transition(LauncherState.RUNNING);

        return LaunchResult.success(LauncherState.RUNNING);
    }

    private OperationResult executeOperation(
            LauncherState state,
            OperationType operationType,
            LaunchContext context
    ) {

        stateMachine.transition(state);

        return operationManager.execute(operationType, context);
    }

    private LaunchResult operationFailureResult(
            OperationType operationType,
            OperationResult result
    ) {
        LaunchFailure failure = LaunchFailure.operation(
                operationType,
                result.failure().orElseThrow()
        );

        stateMachine.transition(LauncherState.FAILED);

        return LaunchResult.failure(
                LauncherState.FAILED,
                failure
        );
    }

    private LaunchResult lifecycleFailureResult(
            String message
    ) {
        stateMachine.transition(LauncherState.FAILED);

        return LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle(message)
        );
    }
}
