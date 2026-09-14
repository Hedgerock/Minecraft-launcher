package com.launcher.app.runtime.detection;

import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaRuntimeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultJavaRuntimeVersionDetectorTest {

    @Test
    void should_fail_when_java_process_output_is_empty() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        commandRunner.setResult(
                new JavaRuntimeVersionCommandResult(0, " ")
        );

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath("runtime/java/bin/java");

        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //when & then
        JavaRuntimeVersionDetectionException exception = assertThrows(
                JavaRuntimeVersionDetectionException.class,
                () -> detector.detect(request)
        );

        assertEquals(
                "Java process returned empty output",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_parser_cannot_parse_output() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        commandRunner.setResult(
                new JavaRuntimeVersionCommandResult(0, "some weird output")
        );

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath("runtime/java/bin/java");

        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //when & then
        assertThrows(
                JavaRuntimeVersionParsingException.class,
                () -> detector.detect(request)
        );
    }

    @Test
    void should_fail_when_process_exits_with_non_zero_code() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        commandRunner.setResult(
                new JavaRuntimeVersionCommandResult(1, "process failed")
        );

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath("runtime/java/bin/java");

        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //when & then
        JavaRuntimeVersionDetectionException exception = assertThrows(
                JavaRuntimeVersionDetectionException.class,
                () -> detector.detect(request)
        );

        assertEquals(
                "Java process exited with code 1",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_non_explicit_request() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        JavaExecutableReference reference =
                JavaExecutableReference.commandName("java");

        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //when & then
        JavaRuntimeVersionDetectionException exception = assertThrows(
                JavaRuntimeVersionDetectionException.class,
                () -> detector.detect(request)
        );

        assertEquals(
                "Java executable reference must be an explicit path",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_request() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> detector.detect(null)
        );

        assertEquals(
                "request",
                exception.getMessage()
        );
    }

    @Test
    void should_detect_java_runtime_version_from_command_output() {
        //given
        RecordingJavaRuntimeVersionCommandRunner commandRunner =
                new RecordingJavaRuntimeVersionCommandRunner();

        DefaultJavaRuntimeVersionDetector detector = new DefaultJavaRuntimeVersionDetector(
                commandRunner,
                new JavaRuntimeVersionOutputParser()
        );

        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath("runtime/java/bin/java");

        JavaRuntimeVersionDetectionRequest request =
                new JavaRuntimeVersionDetectionRequest(reference);

        //when
        JavaRuntimeVersion result = detector.detect(request);

        //then
        assertEquals(
                new JavaRuntimeVersion(21),
                result
        );

        assertEquals(
                reference,
                commandRunner.getJavaExecutableReference()
        );
    }
}
