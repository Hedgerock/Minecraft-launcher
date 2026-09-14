package com.launcher.core;

import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchResultTest {

    @Test
    void should_create_failure_result() {
        //given & when
        LaunchResult result = LaunchResult.failure(LauncherState.FAILED);

        //then
        assertEquals(
                LauncherState.FAILED,
                result.finalState()
        );

        assertFalse(result.success());
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
    }

    @Test
    void should_reject_null_final_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchResult(true, null)
        );

        assertEquals(
                "finalState",
                exception.getMessage()
        );
    }
}
