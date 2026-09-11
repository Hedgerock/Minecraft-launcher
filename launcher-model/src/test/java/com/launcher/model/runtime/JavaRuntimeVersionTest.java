package com.launcher.model.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeVersionTest {

    @Test
    void should_create_java_runtime_version() {
        //given & when
        JavaRuntimeVersion version = new JavaRuntimeVersion(21);

        //then
        assertEquals(21, version.majorVersion());
    }

    @Test
    void should_reject_zero_major_version() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaRuntimeVersion(0)
        );

        assertEquals(
                "majorVersion must be positive",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_negative_major_version() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaRuntimeVersion(-1)
        );

        assertEquals(
                "majorVersion must be positive",
                exception.getMessage()
        );
    }
}
