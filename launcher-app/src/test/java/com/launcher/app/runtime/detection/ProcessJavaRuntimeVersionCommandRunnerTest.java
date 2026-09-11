package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessJavaRuntimeVersionCommandRunnerTest {
    private final ProcessJavaRuntimeVersionCommandRunner commandRunner = new ProcessJavaRuntimeVersionCommandRunner();

    @TempDir
    Path tempDir;

    @Test
    void should_reject_null_java_executable_reference() {
        //when & then
        assertThrows(
                NullPointerException.class,
                () -> commandRunner.run(null)
        );
    }

    @Test
    void should_capture_version_output_from_stderr() throws IOException {
        //given
        Path executable = createCommandWritingVersionToStdErr();

        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath(executable.toString());

        //when
        JavaRuntimeVersionCommandResult result = commandRunner.run(reference);

        //then
        assertEquals(
                0,
                result.exitCode()
        );

        assertTrue(
                result.output().contains("openjdk version \"21.0.8\"")
        );
    }

    private Path createCommandWritingVersionToStdErr() throws IOException {
        boolean windows = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win");

        Path executable = tempDir.resolve(
                windows ? "java-version.cmd" : "java-version.sh"
        );

        String content = windows
                ? """
                @echo off
                echo openjdk version "21.0.8" 2025-07-15 1>&2
                exit /b 0
                """
                : """
                #!/usr/bin/env sh
                printf '%s\\n' 'openjdk version "21.0.8" 2025-07-15' >&2
                exit 0
                """;

        Files.writeString(executable, content, StandardCharsets.UTF_8);

        if (!windows) {
            executable.toFile().setExecutable(true);
        }

        return executable;
    }
}
