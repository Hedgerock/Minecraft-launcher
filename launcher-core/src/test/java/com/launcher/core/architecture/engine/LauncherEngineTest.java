package com.launcher.core.architecture.engine;

import com.launcher.core.LaunchFailure;
import com.launcher.core.LaunchResult;
import com.launcher.core.architecture.support.fixture.LauncherFlowFixture;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LauncherEngineTest {
    private LauncherFlowFixture launcherFlowFixture;

    @BeforeEach
    void setUp() {
        launcherFlowFixture = new LauncherFlowFixture(
                new LauncherStateMachine(
                        new EventBus()
                )
        );
    }

    @Test
    void should_preserve_structured_failure_context_from_operation_failure() {
        //given
        OperationFailure expectedOperationFailure = new OperationFailure(
                OperationFailureCode.UNKNOWN,
                "Failed to verify files",
                Map.of(
                        "FirstDetail", "FirstDetailValue",
                        "SecondDetail", "SecondDetailValue"
                )
        );

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationFailed(OperationType.VERIFY_FILES, expectedOperationFailure)
                //when
                .launch();

        //then
        LaunchFailure failure = result.failure().orElseThrow();

        assertEquals(
                expectedOperationFailure,
                failure.operationFailure().orElseThrow()
        );
    }

    @Test
    void should_transition_to_failed_when_extract_natives_failed() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationFailed(OperationType.EXTRACT_NATIVES, "Failed to extract natives")
        //when
                .launch();

        //then

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.EXTRACT_NATIVES,
                "Failed to extract natives"
        );
    }

    @Test
    void should_extract_natives_after_prepare_directories_when_download_resources_are_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationSucceeds(OperationType.LAUNCH_GAME)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.RUNNING, launcherFlowFixture.getCurrentState());

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );

        assertTrue(result.failure().isEmpty());
    }

    @Test
    void should_extract_natives_after_prepare_directories_when_resources_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationSucceeds(OperationType.LAUNCH_GAME)
        //when
                .launch();

        //when
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.RUNNING, launcherFlowFixture.getCurrentState());

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_build_game_launch_plan_after_extract_natives_operation() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("invalid.jar", VerificationStatus.MISSING);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                //when
                .failOperationAndLaunch(OperationType.BUILD_GAME_LAUNCH_PLAN);

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN
                ),
                launcherFlowFixture.getExecutedOperations()
        );
    }

    @Test
    void should_transition_to_failed_when_build_game_launch_plan_failed() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("invalid.jar", VerificationStatus.MISSING);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationFailed(OperationType.BUILD_GAME_LAUNCH_PLAN, "Failed to build game launch plan")
                //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.BUILD_GAME_LAUNCH_PLAN,
                "Failed to build game launch plan"
        );
    }

    @Test
    void should_transition_to_failed_when_launch_game_failed() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("invalid.jar", VerificationStatus.MISSING);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationFailed(OperationType.LAUNCH_GAME, "Failed to launch game")
                //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.LAUNCH_GAME,
                "Failed to launch game"
        );
    }

    @Test
    void should_launch_game_before_running_when_downloaded_resources_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.MISSING);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(LauncherFlowFixture.downloadPlan(notValidVerificationPlan))
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationSucceeds(OperationType.LAUNCH_GAME)
                //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_launch_game_before_running_when_resources_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationSucceeds(OperationType.LAUNCH_GAME)
                //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_prepare_directories_before_running_when_resources_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_prepare_directories_before_running_when_downloaded_resources_are_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_transition_to_failed_when_prepare_directories_failed() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationFailed(OperationType.PREPARE_DIRECTORIES, "Failed to prepare directories")
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.PREPARE_DIRECTORIES,
                "Failed to prepare directories"
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_failed() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationFailed(OperationType.VERIFY_FILES, "Failed to verify files")
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.VERIFY_FILES,
                "Failed to verify files"
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_null() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .verifyFilesReturns(null)
                //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertLifecycleFailure(
                result,
                "Post-download verification plan is missing"
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_not_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertLifecycleFailure(
                result,
                "Post-download verification plan is invalid"
        );
    }

    @Test
    void should_transition_to_failed_when_download_resources_failed() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationFailed(OperationType.DOWNLOAD_FILES, "Failed to download files")
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.DOWNLOAD_FILES,
                "Failed to download files"
        );
    }

    @Test
    void should_transition_to_running_when_downloaded_resources_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .verifyFilesReturns(validVerificationPlan)
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    @Test
    void should_verify_resources_again_after_download_resources_succeeded() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
        //when
                .failOperationAndLaunch (OperationType.VERIFY_FILES);

        //then
        assertEquals( List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationType.DOWNLOAD_FILES,
                OperationType.VERIFY_FILES
        ), launcherFlowFixture.getExecutedOperations());
    }

    @Test
    void should_download_resources_when_verification_plan_is_not_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
        //when
                .failOperationAndLaunch(OperationType.DOWNLOAD_FILES);

        //then
        assertEquals(List.of(
                OperationType.LOAD_MANIFEST,
                OperationType.VERIFY_FILES,
                OperationType.BUILD_DOWNLOAD_PLAN,
                OperationType.DOWNLOAD_FILES
        ), launcherFlowFixture.getExecutedOperations());

    }

    @Test
    void should_transition_to_failed_when_build_download_plan_failed() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationFailed(OperationType.BUILD_DOWNLOAD_PLAN, "Failed to build download plan")
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.BUILD_DOWNLOAD_PLAN,
                "Failed to build download plan"
        );
    }

    @Test
    void should_execute_build_download_plan_when_verification_plan_is_not_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
        //when
                .failOperationAndLaunch(OperationType.BUILD_DOWNLOAD_PLAN);

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());
    }


    @Test
    void should_execute_load_manifest_then_verify_resources_when_launch_started() {
        //given
        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
        //when
                .failOperationAndLaunch(OperationType.VERIFY_FILES);

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES
                ),
                launcherFlowFixture.getExecutedOperations()
        );
    }

    @Test
    void should_transition_to_failed_when_load_manifest_failed() {
        //given
        LaunchResult result = launcherFlowFixture
                .operationFailed(OperationType.LOAD_MANIFEST, "Failed to load manifest")
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.LOAD_MANIFEST,
                "Failed to load manifest"
        );
    }

    @Test
    void should_transition_to_failed_when_verify_resources_failed() {
        //given
        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationFailed(OperationType.VERIFY_FILES, "Failed to verify files")
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertOperationFailure(
                result,
                OperationType.VERIFY_FILES,
                "Failed to verify files"
        );
    }

    @Test
    void should_transition_to_failed_when_verification_plan_is_not_stored() {
        //given
        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(null)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.FAILED, launcherFlowFixture.getCurrentState());

        assertFalse(result.success());

        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertLifecycleFailure(
                result,
                "Verification plan is missing"
        );
    }

    @Test
    void should_not_build_download_plan_when_verification_plan_is_valid() {
        //given
        VerificationPlan verificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(verificationPlan)
        //when
                .failOperationAndLaunch(OperationType.PREPARE_DIRECTORIES);

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES
                ),
                launcherFlowFixture.getExecutedOperations()
        );
    }

    @Test
    void should_transition_to_running_when_manifest_loaded_and_resources_are_valid() {
        //given
        VerificationPlan verificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        LaunchResult result = launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(verificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.EXTRACT_NATIVES)
                .operationSucceeds(OperationType.BUILD_GAME_LAUNCH_PLAN)
                .operationSucceeds(OperationType.LAUNCH_GAME)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.EXTRACT_NATIVES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.RUNNING, launcherFlowFixture.getCurrentState());

        assertTrue(result.success());

        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );
    }

    private void assertOperationFailure(
            LaunchResult result,
            OperationType expectedOperationType,
            String expectedMessage
    ) {
        LaunchFailure failure = result.failure().orElseThrow();

        assertEquals(
                expectedOperationType,
                failure.operationType().orElseThrow()
        );

        OperationFailure operationFailure =
                failure.operationFailure().orElseThrow();

        assertEquals(expectedMessage, operationFailure.message());
        assertEquals(expectedMessage, failure.message());
    }

    private void assertLifecycleFailure(
            LaunchResult launchResult,
            String expectedMessage
    ) {
        LaunchFailure failure = launchResult.failure().orElseThrow();

        assertTrue(failure.operationType().isEmpty());
        assertTrue(failure.operationFailure().isEmpty());
        assertEquals(expectedMessage, failure.message());
    }
}
