package com.launcher.core.architecture.state;

import com.launcher.core.architecture.support.recording.RecordingLauncherStateMachine;
import com.launcher.core.architecture.support.fixture.LauncherFlowFixture;
import com.launcher.core.event.EventBus;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherStateMachineTest {
    private LauncherFlowFixture launcherFlowFixture;

    @BeforeEach
    void setUp() {
        launcherFlowFixture = new LauncherFlowFixture(
                new RecordingLauncherStateMachine(
                        new EventBus()
                )
        );
    }

    @Test
    void should_transition_to_failed_when_verification_after_download_is_not_valid() {
        //given
        VerificationPlan invalidPlan = LauncherFlowFixture
                .verificationPlan("current-file.jar", VerificationStatus.MISSING);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(invalidPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .verifyFilesReturns(invalidPlan)
        //when
                .launch();

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
                launcherFlowFixture.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES,
                        OperationType.VERIFY_FILES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_through_download_flow_when_files_are_missing_and_download_succeeds() {
        //given
        VerificationPlan invalidPlan = LauncherFlowFixture
                .verificationPlan("current-file.jar", VerificationStatus.MISSING);

        VerificationPlan validPlan = LauncherFlowFixture
                .verificationPlan("current-file.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(invalidPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .operationSucceeds(OperationType.DOWNLOAD_FILES)
                .verifyFilesReturns(validPlan)
        //when
                .launch();

        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.BUILDING_DOWNLOAD_PLAN,
                        LauncherState.DOWNLOADING,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.PREPARING_GAME,
                        LauncherState.RUNNING
                ),
                launcherFlowFixture.getTransitions()
        );

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
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
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
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.FAILED
                ),
                launcherFlowFixture.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_download_files_failed() {
        //given
        VerificationPlan invalidPlan =
                LauncherFlowFixture.verificationPlan("current-file.jar", VerificationStatus.MISSING);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(invalidPlan)
                .operationSucceeds(OperationType.BUILD_DOWNLOAD_PLAN)
                .operationFailed(OperationType.DOWNLOAD_FILES, "Failed to download files")
                //when
                .launch();
        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.BUILDING_DOWNLOAD_PLAN,
                        LauncherState.DOWNLOADING,
                        LauncherState.FAILED
                ),
                launcherFlowFixture.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.BUILD_DOWNLOAD_PLAN,
                        OperationType.DOWNLOAD_FILES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.FAILED,
                launcherFlowFixture.getCurrentState()
        );
    }

    @Test
    void should_transition_to_running_without_download_when_files_are_valid() {
        //given
        VerificationPlan verificationPlan =
                LauncherFlowFixture.verificationPlan("current-file.jar", VerificationStatus.VALID);

        launcherFlowFixture
                .operationSucceeds(OperationType.LOAD_MANIFEST)
                .operationSucceeds(OperationType.VERIFY_FILES)
                .verifyFilesReturns(verificationPlan)
        //when
                .launch();
        //then
        assertEquals(
                List.of(
                        LauncherState.LOADING_MANIFEST,
                        LauncherState.VERIFYING_FILES,
                        LauncherState.PREPARING_GAME,
                        LauncherState.RUNNING
                ),
                launcherFlowFixture.getTransitions()
        );

        assertEquals(
                List.of(
                        OperationType.LOAD_MANIFEST,
                        OperationType.VERIFY_FILES,
                        OperationType.PREPARE_DIRECTORIES
                ),
                launcherFlowFixture.getExecutedOperations()
        );

        assertEquals(
                LauncherState.RUNNING,
                launcherFlowFixture.getCurrentState()
        );
    }
}
