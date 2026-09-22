package com.launcher.ui;

import com.launcher.ui.state.PresentationLaunchState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationLaunchStatusTextTest {

    @Test
    void should_return_failed_text_for_failed_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.FAILED);

        //then
        assertEquals("Launch failed", result);
    }

    @Test
    void should_return_launched_text_for_launched_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.LAUNCHED);

        //then
        assertEquals("Game launched", result);
    }

    @Test
    void should_return_launching_text_for_launching_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.LAUNCHING);

        //then
        assertEquals("Launching...", result);
    }

    @Test
    void should_return_ready_text_for_ready_state() {
        //given & when
        String result = PresentationLaunchStatusText.forState(PresentationLaunchState.READY);

        //then
        assertEquals("Ready", result);
    }

    @Test
    void should_reject_null_presentation_launch_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationLaunchStatusText.forState(null)
        );

        assertEquals(
                "state",
                exception.getMessage()
        );
    }
}
