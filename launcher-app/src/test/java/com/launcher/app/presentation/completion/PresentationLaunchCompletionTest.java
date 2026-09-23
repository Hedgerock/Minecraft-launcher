package com.launcher.app.presentation.completion;

import com.launcher.core.LaunchFailure;
import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationLaunchCompletionTest {

    @Test
    void should_mark_execution_failure_without_launch_result() {
        //given & when
        PresentationLaunchCompletion result = PresentationLaunchCompletion.executionFailure();

        //then
        assertTrue(result.executionFailed());
        assertFalse(result.launchResult().isPresent());
    }

    @Test
    void should_not_mark_execution_failure_with_failed_launch_result() {
        //given
        LaunchResult expectedLaunchResult = LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle("Something went wrong")
        );

        //when
        PresentationLaunchCompletion result = PresentationLaunchCompletion
                .fromLaunchResult(expectedLaunchResult);

        //then
        assertFalse(result.executionFailed());

        assertSame(
                expectedLaunchResult,
                result.launchResult().orElseThrow()
        );
    }

    @Test
    void should_not_mark_execution_failure_with_successful_launch_result() {
        //given
        LaunchResult expectedLaunchResult = LaunchResult.success(LauncherState.RUNNING);

        //when
        PresentationLaunchCompletion result = PresentationLaunchCompletion
                .fromLaunchResult(expectedLaunchResult);

        //then
        assertFalse(result.executionFailed());

        assertSame(
                expectedLaunchResult,
                result.launchResult().orElseThrow()
        );
    }

    @Test
    void should_reject_null_launch_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationLaunchCompletion.fromLaunchResult(null)
        );

        assertEquals("launchResult", exception.getMessage());
    }
}
