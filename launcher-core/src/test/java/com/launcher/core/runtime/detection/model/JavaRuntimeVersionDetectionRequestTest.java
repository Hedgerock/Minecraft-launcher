package com.launcher.core.runtime.detection.model;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeVersionDetectionRequestTest {

    @Test
    void should_create_java_runtime_version_detection_request() {
        //given & when
        JavaExecutableReference reference = JavaExecutableReference.explicitPath("runtime/java/bin/java");
        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //then
        assertEquals(
                reference,
                request.resolvedJavaExecutableReference()
        );
    }

    @Test
    void should_reject_null_java_executable_reference() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeVersionDetectionRequest(null)
        );

        assertEquals(
                "resolvedJavaExecutableReference",
                exception.getMessage()
        );
    }
}
