package com.launcher.core.runtime.compatibility.model;

import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaVersionRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeCompatibilityRequestTest {
    private static final JavaExecutableReference DEFAULT_RESOLVED_JAVA_EXECUTABLE_REFERENCE =
            JavaExecutableReference.explicitPath("runtime/java/bin/java");

    private static final JavaVersionRequirement DEFAULT_JAVA_VERSION_REQUIREMENT =
            new JavaVersionRequirement(17);

    @Test
    void should_create_request() {
        //given & when
        JavaRuntimeCompatibilityRequest result = new JavaRuntimeCompatibilityRequest(
                DEFAULT_RESOLVED_JAVA_EXECUTABLE_REFERENCE,
                DEFAULT_JAVA_VERSION_REQUIREMENT
        );

        //then
        assertEquals(
                DEFAULT_RESOLVED_JAVA_EXECUTABLE_REFERENCE,
                result.resolvedJavaExecutableReference()
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
                        DEFAULT_RESOLVED_JAVA_EXECUTABLE_REFERENCE,
                        null
                )
        );

        assertEquals("javaVersionRequirement", exception.getMessage());
    }

    @Test
    void should_reject_null_resolved_java_executable_reference() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeCompatibilityRequest(
                        null,
                        DEFAULT_JAVA_VERSION_REQUIREMENT
                )
        );

        assertEquals("resolvedJavaExecutableReference", exception.getMessage());
    }
}
