package com.launcher.ui;

import com.launcher.ui.startup.PresentationStartupState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationStartupStatusTextTest {

    @Test
    void should_return_safe_message_for_configuration_failed_state() {
        //given
        PresentationStartupState startupState = PresentationStartupState.CONFIGURATION_FAILED;

        //when
        String result = PresentationStartupStatusText.forState(startupState);

        //then
        assertEquals(
                "Could not load launch configuration",
                result
        );
    }

    @Test
    void should_return_empty_message_for_available_state() {
        //given
        PresentationStartupState startupState = PresentationStartupState.AVAILABLE;

        //when
        String result = PresentationStartupStatusText.forState(startupState);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_empty_message_for_idle_state() {
        //given
        PresentationStartupState startupState = PresentationStartupState.IDLE;

        //when
        String result = PresentationStartupStatusText.forState(startupState);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_reject_null_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationStartupStatusText.forState(null)
        );

        assertEquals("state", exception.getMessage());
    }
}
