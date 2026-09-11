package com.launcher.core.runtime.detection;

import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaRuntimeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoOpJavaRuntimeVersionDetectorTest {
    private final NoOpJavaRuntimeVersionDetector detector = new NoOpJavaRuntimeVersionDetector();

    @Test
    void should_return_placeholder_java_runtime_version() {
        //given
        JavaRuntimeVersionDetectionRequest request = new JavaRuntimeVersionDetectionRequest(
                JavaExecutableReference.explicitPath("runtime/java/bin/java")
        );

        //when
        JavaRuntimeVersion result = detector.detect(request);

        //then
        assertEquals(
                new JavaRuntimeVersion(17),
                result
        );
    }

    @Test
    void should_reject_null_request() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> detector.detect(null)
        );

        assertEquals("request", exception.getMessage());
    }
}
