package com.launcher.core.runtime.model;

import com.launcher.model.manifest.LaunchInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeSelectionRequestTest {
    private static final LaunchInfo DEFAULT_LAUNCH_INFO = new LaunchInfo(
            "MainClass",
            List.of(),
            List.of(),
            List.of("path", "to", "class"),
            "java-custom"
    );

    private static final String DEFAULT_JAVA_EXECUTABLE_OVERRIDE = "DefaultValue";

    @Test
    void should_create_manifest_only_request() {
        //given & when
        JavaRuntimeSelectionRequest request = JavaRuntimeSelectionRequest.fromManifest(
                DEFAULT_LAUNCH_INFO
        );

        //then
        assertEquals(
                Optional.empty(),
                request.javaExecutableOverride()
        );
    }

    @Test
    void should_create_java_runtime_selection_request_with_java_executable_override() {
        //given & when
        JavaRuntimeSelectionRequest request = new JavaRuntimeSelectionRequest(
                DEFAULT_LAUNCH_INFO,
                Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE)
        );

        //then
        assertEquals(
                Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE),
                request.javaExecutableOverride()
        );
    }

    @Test
    void should_create_java_runtime_selection_request_without_java_executable_override() {
        //given & when
        JavaRuntimeSelectionRequest request =
                new JavaRuntimeSelectionRequest(
                        DEFAULT_LAUNCH_INFO,
                        Optional.empty()
                );

        //then
        assertEquals(
                Optional.empty(),
                request.javaExecutableOverride()
        );
    }

    @Test
    void should_reject_null_java_executable_override() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeSelectionRequest(DEFAULT_LAUNCH_INFO, null)
        );

        assertEquals(
                "javaExecutableOverride",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_launch_info_from_manifest_method() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> JavaRuntimeSelectionRequest.fromManifest(null)
        );

        assertEquals(
                "launchInfo",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_launch_info() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeSelectionRequest(null, Optional.empty())
        );

        assertEquals(
                "launchInfo",
                exception.getMessage()
        );
    }
}
