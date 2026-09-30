package com.launcher.ui;

import com.launcher.ui.startup.PresentationStartupResult;
import com.launcher.ui.startup.PresentationStartupState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationStartupStatusTextTest {

    @Test
    void should_return_safe_message_for_local_configuration_failed_state() {
        //given
        PresentationStartupResult startupResult = PresentationStartupResult.localConfigurationFailed();

        //when
        String result = PresentationStartupStatusText.forResult(startupResult);

        //then
        assertEquals(
                "Check keystone.properties, then press Retry",
                result
        );
    }

    @Test
    void should_return_safe_message_for_configuration_failed_state() {
        //given
        PresentationStartupResult startupResult = PresentationStartupResult.configurationFailed();

        //when
        String result = PresentationStartupStatusText.forResult(startupResult);

        //then
        assertEquals(
                "Could not load launch configuration",
                result
        );
    }

    @Test
    void should_return_empty_message_for_available_state() {
        //given
        PresentationStartupResult startupResult = PresentationStartupResult.available();

        //when
        String result = PresentationStartupStatusText.forResult(startupResult);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_empty_message_for_idle_state() {
        //given
        PresentationStartupResult startupResult = new PresentationStartupResult(
                PresentationStartupState.IDLE,
                false
        );

        //when
        String result = PresentationStartupStatusText.forResult(startupResult);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_reject_null_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationStartupStatusText.forResult(null)
        );

        assertEquals("startupResult", exception.getMessage());
    }
}
