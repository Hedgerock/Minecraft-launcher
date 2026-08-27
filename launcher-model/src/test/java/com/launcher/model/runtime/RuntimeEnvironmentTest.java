package com.launcher.model.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeEnvironmentTest {

    @Test
    void should_create_runtime_environment() {
        //given & when
        RuntimeEnvironment runtimeEnvironment =
                new RuntimeEnvironment(OperatingSystem.WINDOWS);

        //then
        assertEquals(OperatingSystem.WINDOWS, runtimeEnvironment.operatingSystem());
    }

    @Test
    void should_reject_null_operating_system() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeEnvironment(null)
        );

        assertTrue(exception.getMessage().contains("operatingSystem"));
    }

}
