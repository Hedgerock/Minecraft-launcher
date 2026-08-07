package com.launcher.core.architecture.engine;

import com.launcher.core.LauncherEngine;
import com.launcher.core.architecture.support.RecordingManifestService;
import com.launcher.core.architecture.support.RecordingOperationManager;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LauncherEngineTest {
    private final DownloadPlanBuilder builder = new DownloadPlanBuilder();
    private LauncherConfiguration configuration() {
        return new LauncherConfiguration(
                URI.create("currentPath"),
                Path.of("")
        );
    }

    private VerificationPlan getVerificationPlan(String path, VerificationStatus status) {

        return new VerificationPlan(
                List.of(
                        new FileVerificationResult(
                                new FileEntry(
                                        path,
                                        "sha256-" + path,
                                        321L,
                                        "https://test-url.com/" + path
                                ),
                                status
                        )
                )
        );
    }

    private void executeStepsFromLoadManifestToBuildDownloadPlan(
            RecordingOperationManager operationManager,
            VerificationPlan verificationPlan
    ) {
        ManifestService manifestService = new RecordingManifestService();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(verificationPlan)
        );

        operationManager.registerResult(
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.BUILD_DOWNLOAD_PLAN,
                context -> context.setDownloadPlan(builder.build(verificationPlan))
        );
    }

    private LauncherStateMachine stateMachine() {
        return new LauncherStateMachine(new EventBus());
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        RecordingOperationManager operationManager =
                new RecordingOperationManager();


        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.failure("Failed to verify files")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_not_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();

        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        RecordingOperationManager operationManager =
                new RecordingOperationManager();


        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.DOWNLOAD_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(notValidVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_download_files_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();

        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        RecordingOperationManager operationManager =
                new RecordingOperationManager();


        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.DOWNLOAD_FILES,
                OperationResult.failure("Failed to download files")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_running_when_downloaded_files_are_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();

        VerificationPlan validVerificationPlan =
                getVerificationPlan("valid.jar", VerificationStatus.VALID);

        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        RecordingOperationManager operationManager =
                new RecordingOperationManager();


        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.DOWNLOAD_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_verify_files_again_after_download_files_succeeded() {
        //given
        LauncherStateMachine stateMachine = stateMachine();

        VerificationPlan validVerificationPlan =
                getVerificationPlan("valid.jar", VerificationStatus.VALID);

        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        RecordingOperationManager operationManager =
                new RecordingOperationManager();


        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.DOWNLOAD_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationType.DOWNLOAD_FILES,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());
    }

    @Test
    void should_download_files_when_verification_plan_is_not_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);

        executeStepsFromLoadManifestToBuildDownloadPlan(operationManager, notValidVerificationPlan);

        operationManager.registerResult(
                OperationType.DOWNLOAD_FILES,
                OperationResult.failure("stop")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationType.DOWNLOAD_FILES
        ), operationManager.getExecutedOperationTypes());

    }

    @Test
    void should_transition_to_failed_when_build_download_plan_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        ManifestService manifestService = new RecordingManifestService();
        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(notValidVerificationPlan)
        );

        operationManager.registerResult(
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationResult.failure("failed to build download plan")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN
        ), operationManager.getExecutedOperationTypes());

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );

        assertNull(operationManager.getReceivedContext().getDownloadPlan());
    }

    @Test
    void should_execute_build_download_plan_when_verification_plan_is_not_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        ManifestService manifestService = new RecordingManifestService();
        VerificationPlan notValidVerificationPlan =
                getVerificationPlan("not-valid.jar", VerificationStatus.MISSING);
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(notValidVerificationPlan)
        );

        operationManager.registerResult(
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationResult.failure("stop")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN
        ), operationManager.getExecutedOperationTypes());

    }


    @Test
    void should_execute_load_manifest_then_verify_files_when_launch_started() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        ManifestService manifestService = new RecordingManifestService();
        VerificationPlan validVerificationPlan =
                getVerificationPlan("valid.jar", VerificationStatus.VALID);
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());
    }

    @Test
    void should_transition_to_failed_when_load_manifest_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.failure("load manifest failed")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST
        ), operationManager.getExecutedOperationTypes());
        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_verify_files_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        ManifestService manifestService = new RecordingManifestService();
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.failure("failed to verify files")
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_verification_plan_is_not_stored() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager = new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_verification_plan_has_missing_files() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        ManifestService manifestService = new RecordingManifestService();
        VerificationPlan missingVerificationPlan = getVerificationPlan("missing.jar", VerificationStatus.MISSING);
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(missingVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationType.DOWNLOAD_FILES,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());

        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_not_build_download_plan_when_verification_plan_is_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        VerificationPlan validVerificationPlan = getVerificationPlan("valid.jar", VerificationStatus.VALID);
        ManifestService manifestService = new RecordingManifestService();
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES
        ), operationManager.getExecutedOperationTypes());

        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );

        assertNull(operationManager.getReceivedContext().getDownloadPlan());
    }

    @Test
    void should_transition_to_running_when_manifest_loaded_and_files_are_valid() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        VerificationPlan validVerificationPlan = getVerificationPlan("valid.jar", VerificationStatus.VALID);
        ManifestService manifestService = new RecordingManifestService();
        RecordingOperationManager operationManager =
                new RecordingOperationManager();

        operationManager.registerResult(
                OperationType.LOAD_MANIFEST,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.LOAD_MANIFEST,
                context -> context.setManifest(manifestService.loadManifest())
        );

        operationManager.registerResult(
                OperationType.VERIFY_FILES,
                OperationResult.success()
        );

        operationManager.registerBehavior(
                OperationType.VERIFY_FILES,
                context -> context.setVerificationPlan(validVerificationPlan)
        );

        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );
    }

}
