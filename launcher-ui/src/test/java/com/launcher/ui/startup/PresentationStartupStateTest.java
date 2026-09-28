package com.launcher.ui.startup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationStartupStateTest {

    @Test
    void should_allow_launch_when_startup_and_request_are_available() {
        //given
        PresentationStartupState startupState = PresentationStartupState.AVAILABLE;

        //when
        boolean allowed = startupState.allowsLaunch(true);

        //then
        assertTrue(allowed);
    }

    @Test
    void should_not_allow_launch_when_request_is_unavailable() {
        //given
        PresentationStartupState state = PresentationStartupState.AVAILABLE;

        //when
        boolean allowed = state.allowsLaunch(false);

        //then
        assertFalse(allowed);
    }

    @Test
    void should_not_allow_launch_when_configuration_failed() {
        //given
        PresentationStartupState startupState = PresentationStartupState.CONFIGURATION_FAILED;

        //when
        boolean allowed = startupState.allowsLaunch(true);

        //then
        assertFalse(allowed);
    }

    @Test
    void should_not_allow_launch_before_startup_initialization() {
        //given
        PresentationStartupState state = PresentationStartupState.IDLE;

        //when
        boolean allowed = state.allowsLaunch(true);

        //then
        assertFalse(allowed);
    }
}
