package com.launcher.core.runtime.compatibility.model;

import com.launcher.model.runtime.JavaRuntimeVersion;
import com.launcher.model.runtime.JavaVersionRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeCompatibilityRequestTest {
    private static final JavaRuntimeVersion DEFAULT_JAVA_RUNTIME_VERSION =
            new JavaRuntimeVersion(21);

    private static final JavaVersionRequirement DEFAULT_JAVA_VERSION_REQUIREMENT =
            new JavaVersionRequirement(17);

    @Test
    void should_create_request() {
        //given & when
        JavaRuntimeCompatibilityRequest result = new JavaRuntimeCompatibilityRequest(
                DEFAULT_JAVA_RUNTIME_VERSION,
                DEFAULT_JAVA_VERSION_REQUIREMENT
        );

        //then
        assertEquals(
                DEFAULT_JAVA_RUNTIME_VERSION,
                result.javaRuntimeVersion()
        );
        assertEquals(
                DEFAULT_JAVA_VERSION_REQUIREMENT,
                result.javaVersionRequirement()
        );
    }

    @Test
    void should_reject_null_java_version_requirement() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeCompatibilityRequest(
                        DEFAULT_JAVA_RUNTIME_VERSION,
                        null
                )
        );

        assertEquals("javaVersionRequirement", exception.getMessage());
    }

    @Test
    void should_reject_null_java_runtime_version() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeCompatibilityRequest(
                        null,
                        DEFAULT_JAVA_VERSION_REQUIREMENT
                )
        );

        assertEquals("javaRuntimeVersion", exception.getMessage());
    }
}
