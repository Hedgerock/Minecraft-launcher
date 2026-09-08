package com.launcher.core.runtime;

import com.launcher.core.architecture.support.recording.RecordingJavaExecutableReferenceResolver;
import com.launcher.core.runtime.model.JavaRuntimeSelectionRequest;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJavaRuntimeSelectorTest {
    private ManifestJavaRuntimeSelector selector;
    private RecordingJavaExecutableReferenceResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new RecordingJavaExecutableReferenceResolver();
        selector = new ManifestJavaRuntimeSelector(resolver);
    }

    @Test
    void should_reject_null_java_executable_reference_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ManifestJavaRuntimeSelector(null)
        );

        assertEquals(
                "javaExecutableReferenceResolver",
                exception.getMessage()
        );
    }

    @Test
    void should_select_java_executable_from_launch_info() {
        //given
        LaunchInfo launchInfo = new LaunchInfo(
                "MainClass",
                List.of(),
                List.of(),
                List.of("test-classpath.jar"),
                "java-custom"
        );

        JavaRuntimeSelectionRequest request = JavaRuntimeSelectionRequest.fromManifest(launchInfo);

        //when
        JavaExecutableReference result = selector.selectJavaExecutable(request);

        //then
        assertTrue(result.isCommandName());

        assertEquals(
                "java-custom",
                result.value()
        );

        assertEquals(
                "java-custom",
                resolver.getJavaExecutable()
        );
    }

    @Test
    void should_reject_null_java_runtime_selection_request() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> selector.selectJavaExecutable(null)
        );

        assertTrue(exception.getMessage().contains("request"));
    }

}
