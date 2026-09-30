package com.launcher.ui.startup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationStartupResultTest {

    @Test
    void should_return_configuration_failed_presentation_startup_result() {
        //given & when
        PresentationStartupResult result = PresentationStartupResult.configurationFailed();

        //then
        assertEquals(
                PresentationStartupState.CONFIGURATION_FAILED,
                result.state()
        );

        assertFalse(result.retryAvailable());
    }

    @Test
    void should_return_local_configuration_failed_presentation_startup_result() {
        //given & when
        PresentationStartupResult result = PresentationStartupResult.localConfigurationFailed();

        //then
        assertEquals(
                PresentationStartupState.CONFIGURATION_FAILED,
                result.state()
        );

        assertTrue(result.retryAvailable());
    }

    @Test
    void should_return_available_presentation_startup_result() {
        //given & when
        PresentationStartupResult result = PresentationStartupResult.available();

        //then
        assertEquals(
                PresentationStartupState.AVAILABLE,
                result.state()
        );

        assertFalse(result.retryAvailable());
    }

    @Test
    void should_reject_available_retries_without_configuration_failure() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PresentationStartupResult(
                        PresentationStartupState.AVAILABLE,
                        true
                )
        );

        assertEquals("Retry requires configuration failure", exception.getMessage());
    }

    @Test
    void should_reject_null_presentation_startup_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PresentationStartupResult(null, false)
        );

        assertEquals("state", exception.getMessage());
    }
}
