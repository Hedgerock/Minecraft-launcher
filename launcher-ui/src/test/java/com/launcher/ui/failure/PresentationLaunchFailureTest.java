package com.launcher.ui.failure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationLaunchFailureTest {

    @Test
    void should_create_presentation_launch_failure() {
        //given & when
        PresentationLaunchFailure presentationLaunchFailure = new PresentationLaunchFailure("message");

        //then
        assertEquals("message", presentationLaunchFailure.message());
    }

    @Test
    void should_reject_blank_message() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PresentationLaunchFailure(" ")
        );

        assertEquals("message must not be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_message() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PresentationLaunchFailure(null)
        );

        assertEquals("message", exception.getMessage());
    }
}
