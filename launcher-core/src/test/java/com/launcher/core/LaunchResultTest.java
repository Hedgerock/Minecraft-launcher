package com.launcher.core;

import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchResultTest {

    @Test
    void should_create_failure_lifecycle_result_with_context() {
        //given
        LaunchFailure expectedFailure = LaunchFailure.lifecycle("failed to run");

        //when
        LaunchResult result = LaunchResult.failure(
                LauncherState.FAILED,
                expectedFailure
        );

        //then
        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertFalse(result.success());
        assertSame(expectedFailure, result.failure().orElseThrow());
    }

    @Test
    void should_create_success_result() {
        //given & when
        LaunchResult result = LaunchResult.success(LauncherState.RUNNING);

        //then
        assertEquals(
                LauncherState.RUNNING,
                result.finalState()
        );

        assertTrue(result.success());
        assertTrue(result.failure().isEmpty());
    }

    @Test
    void should_reject_null_final_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchResult.success(null)
        );

        assertEquals(
                "finalState",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_final_state_for_failure_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchResult.failure(
                        null,
                        LaunchFailure.lifecycle("Something went wrong")
                )
        );

        assertEquals(
                "finalState",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_launch_failure() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchResult.failure(
                        LauncherState.FAILED,
                        null
                )
        );

        assertEquals(
                "failure",
                exception.getMessage()
        );
    }
}
