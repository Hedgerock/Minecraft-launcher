package com.launcher.ui;

import com.launcher.ui.failure.PresentationLaunchFailure;
import com.launcher.ui.state.PresentationLaunchState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationLaunchStatusTextTest {

    @Test
    void should_reject_empty_presentation_launch_failure_for_failed_state() {
        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> PresentationLaunchStatusText
                        .forState(
                                PresentationLaunchState.FAILED,
                                Optional.empty()
                        )
        );

        assertEquals(
                "Failed requires presentation failure",
                exception.getMessage()
        );
    }

    @Test
    void should_return_failed_text_for_failed_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(
                PresentationLaunchState.FAILED,
                Optional.of(
                        new PresentationLaunchFailure("Something went wrong")
                )
        );

        //then
        assertEquals("Something went wrong", result);
    }

    @Test
    void should_return_launched_text_for_launched_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.LAUNCHED, Optional.empty());

        //then
        assertEquals("Game launched", result);
    }

    @Test
    void should_return_launching_text_for_launching_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.LAUNCHING, Optional.empty());

        //then
        assertEquals("Launching...", result);
    }

    @Test
    void should_return_ready_text_for_ready_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.READY, Optional.empty());

        //then
        assertEquals("Ready", result);
    }

    @Test
    void should_reject_null_failure_optional() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationLaunchStatusText.forState(PresentationLaunchState.READY, null)
        );

        assertEquals(
                "failure",
                exception.getMessage()
        );

    }

    @Test
    void should_reject_null_presentation_launch_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationLaunchStatusText.forState(null, Optional.empty())
        );

        assertEquals(
                "state",
                exception.getMessage()
        );
    }
}
