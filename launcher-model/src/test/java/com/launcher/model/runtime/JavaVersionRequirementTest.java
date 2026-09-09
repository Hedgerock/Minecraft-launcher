package com.launcher.model.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaVersionRequirementTest {

    @Test
    void should_create_java_version_requirement() {
        //given & when
        JavaVersionRequirement javaVersionRequirement = new JavaVersionRequirement(21);

        //then
        assertEquals(21, javaVersionRequirement.minimumMajorVersion());
    }

    @Test
    void should_reject_negative_minimum_major_version() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaVersionRequirement(-1)
        );

        assertEquals(
                "minimumMajorVersion must be positive",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_zero_minimum_major_version() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaVersionRequirement(0)
        );

        assertEquals(
                "minimumMajorVersion must be positive",
                exception.getMessage()
        );
    }
}
