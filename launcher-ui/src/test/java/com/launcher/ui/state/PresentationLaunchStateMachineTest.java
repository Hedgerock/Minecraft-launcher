package com.launcher.ui.state;

import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.core.LaunchFailure;
import com.launcher.core.LaunchResult;
import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.ui.failure.PresentationLaunchFailure;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationLaunchStateMachineTest {

    @Test
    void should_return_specific_message_for_operation_failure() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);
        stateMachine.onLaunchResult(LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.operation(
                        OperationType.LAUNCH_GAME,
                        new OperationFailure(
                                OperationFailureCode.UNKNOWN,
                                "Something went wrong",
                                Map.of()
                        )
                )
        ));

        //then
        assertEquals(
                PresentationLaunchState.FAILED,
                stateMachine.currentState()
        );

        assertLaunchFailure(
                stateMachine,
                "Could not start the game"
        );
    }

    @Test
    void should_reject_null_launch_result() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> stateMachine.onLaunchResult(null)
        );

        assertEquals("result", exception.getMessage());
    }

    @Test
    void should_reject_null_launch_request_result() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> stateMachine.onLaunchRequest(null)
        );

        assertEquals("result", exception.getMessage());
    }

    @Test
    void should_reject_accepted_request_when_launch_is_already_running() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED)
        );

        assertFalse(stateMachine.isLaunchAvailable());
        assertEquals(
                "Launch request cannot be accepted while launch is running",
                exception.getMessage()
        );

        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_reject_launch_result_when_launch_is_not_running() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stateMachine.onLaunchResult(
                        LaunchResult.failure(
                                LauncherState.FAILED,
                                LaunchFailure.lifecycle("Something went wrong")
                        )
                )
        );

        assertTrue(stateMachine.isLaunchAvailable());
        assertEquals(
                "Unexpected state for launch result",
                exception.getMessage()
        );
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_keep_launching_state_when_request_is_rejected() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);

        //when
        stateMachine.onLaunchRequest(
                LaunchRequestResult.REJECTED_ALREADY_RUNNING
        );

        //then
        assertEquals(
                PresentationLaunchState.LAUNCHING,
                stateMachine.currentState()
        );

        assertFalse(stateMachine.isLaunchAvailable());
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_accept_new_request_after_failure() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);
        stateMachine.onLaunchResult(LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle("Something went wrong")
        ));

        assertLaunchFailure(
                stateMachine,
                "Launch failed"
        );

        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);

        //then
        assertEquals(
                PresentationLaunchState.LAUNCHING,
                stateMachine.currentState()
        );

        assertFalse(stateMachine.isLaunchAvailable());
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_accept_new_request_after_success() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);
        stateMachine.onLaunchResult(LaunchResult.success(LauncherState.RUNNING));

        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);

        //then
        assertEquals(
                PresentationLaunchState.LAUNCHING,
                stateMachine.currentState()
        );

        assertFalse(stateMachine.isLaunchAvailable());
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_transition_to_failed_when_launch_fails() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);
        stateMachine.onLaunchResult(LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle("Something went wrong")
        ));

        //then
        assertEquals(
                PresentationLaunchState.FAILED,
                stateMachine.currentState()
        );

        assertLaunchFailure(
                stateMachine,
                "Launch failed"
        );
    }

    @Test
    void should_transition_to_launched_when_launch_succeeds() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);
        stateMachine.onLaunchResult(LaunchResult.success(LauncherState.RUNNING));

        //then
        assertEquals(
                PresentationLaunchState.LAUNCHED,
                stateMachine.currentState()
        );

        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_keep_state_when_request_is_rejected() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.REJECTED_ALREADY_RUNNING);

        //then
        assertEquals(
                PresentationLaunchState.READY,
                stateMachine.currentState()
        );

        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_transition_to_launching_when_request_is_accepted() {
        //given
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //when
        stateMachine.onLaunchRequest(LaunchRequestResult.ACCEPTED);

        //then
        assertEquals(
                PresentationLaunchState.LAUNCHING,
                stateMachine.currentState()
        );

        assertFalse(stateMachine.isLaunchAvailable());
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_make_launch_available_in_ready_state() {
        //given & when
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //then
        assertEquals(
                PresentationLaunchState.READY,
                stateMachine.currentState()
        );

        assertTrue(stateMachine.isLaunchAvailable());
        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    @Test
    void should_start_in_ready_state() {
        //given & when
        PresentationLaunchStateMachine stateMachine = new PresentationLaunchStateMachine();

        //then
        assertEquals(
                PresentationLaunchState.READY,
                stateMachine.currentState()
        );

        assertTrue(stateMachine.launchFailure().isEmpty());
    }

    private void assertLaunchFailure(
            PresentationLaunchStateMachine stateMachine,
            String expectedMessage
    ) {
        assertFalse(stateMachine.launchFailure().isEmpty());

        PresentationLaunchFailure failure = stateMachine.launchFailure().get();

        assertEquals(expectedMessage, failure.message());
    }
}
