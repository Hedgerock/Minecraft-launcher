package com.launcher.core.architecture.support.fixture;

import com.launcher.core.LauncherEngine;
import com.launcher.core.architecture.support.recording.RecordingLauncherStateMachine;
import com.launcher.core.architecture.support.recording.RecordingOperationManager;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlan;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public final class LauncherFlowFixture {

    private final RecordingOperationManager operationManager;
    private final LauncherStateMachine launcherStateMachine;
    private final LauncherEngine launcherEngine;
    private static final DownloadPlanBuilder builder = new DownloadPlanBuilder();

    public LauncherFlowFixture(LauncherStateMachine launcherStateMachine) {
        this.launcherStateMachine = launcherStateMachine;
        this.operationManager = new RecordingOperationManager();
        this.launcherEngine = new LauncherEngine(launcherStateMachine, operationManager);
    }

    public static VerificationPlan verificationPlan(String path, VerificationStatus status) {
        return new VerificationPlan(
                List.of(
                        new FileVerificationResult(
                                new FileEntry(
                                        path,
                                        "sha256-" + path,
                                        321L,
                                        "http://test-path/" + path
                                ),
                                status
                        )
                )
        );
    }

    public static DownloadPlan downloadPlan(VerificationPlan verificationPlan) {
        return builder.build(verificationPlan);
    }

    public static LauncherConfiguration configuration() {
        return new LauncherConfiguration(
                URI.create("current-path"),
                Path.of("")
        );
    }

    public void launch() {
        launcherEngine.launch(configuration());
    }

    public void failOperationAndLaunch(OperationType operationType) {
        operationManager.registerResult(operationType, OperationResult.failure("Stop"));

        launch();
    }

    public LauncherFlowFixture verifyFilesReturns(VerificationPlan verificationPlan) {
        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(verificationPlan));

        return this;
    }

    public LauncherFlowFixture buildDownloadPlanReturns(DownloadPlan downloadPlan) {

        operationManager.registerBehavior(OperationType.BUILD_DOWNLOAD_PLAN,
                context -> context.setDownloadPlan(downloadPlan)
        );

        return this;
    }

    public LauncherFlowFixture operationSucceeds(OperationType operationType) {
        operationManager.registerResult(operationType, OperationResult.success());

        return this;
    }

    public LauncherFlowFixture operationFailed(OperationType operationType, String message) {
        operationManager.registerResult(operationType, OperationResult.failure(message));

        return this;
    }

    public List<OperationType> getExecutedOperations() {
        return operationManager.getExecutedOperationTypes();
    }

    public List<LauncherState> getTransitions() {
        if (launcherStateMachine instanceof RecordingLauncherStateMachine) {
            return ((RecordingLauncherStateMachine) launcherStateMachine).getTransitions();
        }

        return List.of();
    }

    public LauncherState getCurrentState() {
        return launcherStateMachine.getCurrentState();
    }

}
