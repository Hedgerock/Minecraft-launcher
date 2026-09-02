package com.launcher.core.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJavaExecutableReadinessCheckerTest {
    private final DefaultJavaExecutableReadinessChecker checker =
            new DefaultJavaExecutableReadinessChecker();

    @Test
    void should_reject_directory_as_java_executable(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path javaExecutable = tempDir.resolve("directory-path");

        Files.createDirectory(javaExecutable);

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(javaExecutable)
        );

        assertTrue(exception.getMessage().contains("Java executable is not a file: " + javaExecutable));
    }

    @Test
    void should_reject_missing_java_executable(@TempDir Path tempDir) {
        //given
        Path javaExecutable = tempDir.resolve("fake-path");

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(javaExecutable)
        );

        assertTrue(exception.getMessage().contains("Java executable does not exist: " + javaExecutable));
    }

    @Test
    void should_accept_existing_regular_file(@TempDir Path tempDir) throws IOException {
        //given
        Path javaExecutable = tempDir.resolve("java-executable");

        Files.createFile(javaExecutable);

        //when & then
        assertDoesNotThrow(() -> checker.checkReady(javaExecutable));
    }

    @Test
    void should_reject_null_java_executable() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checker.checkReady(null)
        );

        assertTrue(exception.getMessage().contains("javaExecutable"));
    }

}
