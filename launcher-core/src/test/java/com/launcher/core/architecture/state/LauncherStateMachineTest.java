package com.launcher.core.architecture.state;

import com.launcher.core.LauncherEngine;
import com.launcher.core.architecture.support.RecordingLauncherStateMachine;
import com.launcher.core.architecture.support.RecordingOperationManager;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherStateMachineTest {

    private VerificationPlan getVerificationPlan(VerificationStatus status) {
        return new VerificationPlan(List.of(
                new FileVerificationResult(
                        new FileEntry(
                                "current-file.jar",
                                "sha-256-current-file.jar",
                                12345L,
                                "https://test-url.com/current-file.jar"
                        ),
                        status
                )
        ));
    }

    private RecordingLauncherStateMachine getStateMachine() {
        EventBus eventBus = new EventBus();
        return new RecordingLauncherStateMachine(eventBus);
    }

    private LauncherConfiguration getConfig() {
        return new LauncherConfiguration(
                URI.create("currentPath"),
                Path.of("")
        );

    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_not_valid() {
        //given
        RecordingOperationManager operationManager = new RecordingOperationManager();
        RecordingLauncherStateMachine stateMachine = getStateMachine();

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);
        LauncherConfiguration configuration = getConfig();

        VerificationPlan invalidPlan = getVerificationPlan(VerificationStatus.MISSING);

        operationManager.registerResult(OperationType.LOAD_MANIFEST, OperationResult.success());
        operationManager.registerResult(OperationType.VERIFY_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(invalidPlan));

        operationManager.registerResult(OperationType.BUILD_DOWNLOAD_PLAN, OperationResult.success());
        operationManager.registerResult(OperationType.DOWNLOAD_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(invalidPlan));

        //when
        engine.launch(configuration);

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.BUILDING_DOWNLOAD_PLAN,
                        LauncherState.DOWNLOADING,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.FAILED
                ),
                stateMachine.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES
                ),
                operationManager.getExecutedOperationTypes()
        );

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_through_download_flow_when_files_are_missing_and_download_succeeds() {
        //given
        RecordingOperationManager operationManager = new RecordingOperationManager();
        RecordingLauncherStateMachine stateMachine = getStateMachine();

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);
        LauncherConfiguration configuration = getConfig();

        VerificationPlan invalidPlan = getVerificationPlan(VerificationStatus.MISSING);
        VerificationPlan validPlan = getVerificationPlan(VerificationStatus.VALID);

        operationManager.registerResult(OperationType.LOAD_MANIFEST, OperationResult.success());
        operationManager.registerResult(OperationType.VERIFY_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(invalidPlan));

        operationManager.registerResult(OperationType.BUILD_DOWNLOAD_PLAN, OperationResult.success());
        operationManager.registerResult(OperationType.DOWNLOAD_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validPlan));

        //when
        engine.launch(configuration);

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.BUILDING_DOWNLOAD_PLAN,
                        LauncherState.DOWNLOADING,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.RUNNING
                ),
                stateMachine.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES
                ),
                operationManager.getExecutedOperationTypes()
        );

        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_load_manifest_failed() {
        //given
        RecordingOperationManager operationManager = new RecordingOperationManager();
        RecordingLauncherStateMachine stateMachine = getStateMachine();

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);
        LauncherConfiguration configuration = getConfig();

        operationManager.registerResult(OperationType.LOAD_MANIFEST, OperationResult.failure(
                "Failed to load manifest"
        ));

        //when
        engine.launch(configuration);

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.FAILED
                ),
                stateMachine.getTransitions()
        );

        assertEquals(
                List.of(OperationType.LOAD_MANIFEST),
                operationManager.getExecutedOperationTypes()
        );

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_download_files_failed() {
        //given
        RecordingOperationManager operationManager = new RecordingOperationManager();
        RecordingLauncherStateMachine stateMachine = getStateMachine();

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);
        LauncherConfiguration configuration = getConfig();
        VerificationPlan verificationPlan = getVerificationPlan(VerificationStatus.MISSING);

        operationManager.registerResult(OperationType.LOAD_MANIFEST, OperationResult.success());
        operationManager.registerResult(OperationType.VERIFY_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(verificationPlan));

        operationManager.registerResult(OperationType.BUILD_DOWNLOAD_PLAN, OperationResult.success());
        operationManager.registerResult(OperationType.DOWNLOAD_FILES,
                OperationResult.failure("Failed to download files"));

        //when
        engine.launch(configuration);

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.BUILDING_DOWNLOAD_PLAN,
                        LauncherState.DOWNLOADING,
                        LauncherState.FAILED
                ),
                stateMachine.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES
                ),
                operationManager.getExecutedOperationTypes()
        );

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );

    }

    @Test
    void should_transition_to_running_without_download_when_files_are_valid() {
        //given
        RecordingOperationManager operationManager = new RecordingOperationManager();
        RecordingLauncherStateMachine stateMachine = getStateMachine();

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);
        LauncherConfiguration configuration = getConfig();
        VerificationPlan verificationPlan = getVerificationPlan(VerificationStatus.VALID);

        operationManager.registerResult(OperationType.LOAD_MANIFEST, OperationResult.success());
        operationManager.registerResult(OperationType.VERIFY_FILES, OperationResult.success());

        operationManager.registerBehavior(OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(verificationPlan));

        //when
        engine.launch(configuration);

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.RUNNING
                ),
                stateMachine.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES
                ),
                operationManager.getExecutedOperationTypes()
        );

        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );
    }
}
