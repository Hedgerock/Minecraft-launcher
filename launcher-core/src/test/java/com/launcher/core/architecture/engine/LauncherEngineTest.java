package com.launcher.core.architecture.engine;

import com.launcher.core.architecture.support.fixture.LauncherFlowFixture;
import com.launcher.core.download.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void should_build_game_launch_plan_after_prepare_directories_operation() {
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
                        OperationType.BUILD_GAME_LAUNCH_PLAN
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_launch_game_failed() {
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
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_launch_game_before_running_when_downloaded_files_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.MISSING);

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
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_launch_game_before_running_when_files_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
                .operationSucceeds(OperationType.LAUNCH_GAME)
                //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_prepare_directories_before_running_when_files_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_prepare_directories_before_running_when_downloaded_files_are_valid() {
        //given
        VerificationPlan notValidVerificationPlan =
                LauncherFlowFixture.verificationPlan("not-valid.jar", VerificationStatus.MISSING);

        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        DownloadPlan downloadPlan = LauncherFlowFixture.downloadPlan(notValidVerificationPlan);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationSucceeds(OperationType.PREPARE_DIRECTORIES)
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
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
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

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(notValidVerificationPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .buildDownloadPlanReturns(downloadPlan)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(validVerificationPlan)
                .operationFailed(OperationType.PREPARE_DIRECTORIES, "Failed to prepare directories")
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
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_failed() {
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
                .operationFailed(OperationType.VERIFY_FILES, "Failed to verify files")
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_not_valid() {
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
                .verifyFilesReturns(notValidVerificationPlan)
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_download_files_failed() {
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
                .operationFailed(OperationType.DOWNLOAD_FILES, "Failed to download files")
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_running_when_downloaded_files_are_valid() {
        //given
        VerificationPlan validVerificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);
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
                .verifyFilesReturns(validVerificationPlan)
        //when
                .launch();

        //then
        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_verify_files_again_after_download_files_succeeded() {
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
    void should_download_files_when_verification_plan_is_not_valid() {
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

        launcherFlowFixture
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
    void should_execute_load_manifest_then_verify_files_when_launch_started() {
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
        launcherFlowFixture
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
    }

    @Test
    void should_transition_to_failed_when_verify_files_failed() {
        //given
        launcherFlowFixture
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
    }

    @Test
    void should_transition_to_failed_when_verification_plan_is_not_stored() {
        //given
        launcherFlowFixture
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
    void should_transition_to_running_when_manifest_loaded_and_files_are_valid() {
        //given
        VerificationPlan verificationPlan =
                LauncherFlowFixture.verificationPlan("valid.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(verificationPlan)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES,
                        OperationType.BUILD_GAME_LAUNCH_PLAN,
                        OperationType.LAUNCH_GAME
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(LauncherState.RUNNING, launcherFlowFixture.getCurrentState());
    }

}
