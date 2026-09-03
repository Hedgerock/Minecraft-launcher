package com.launcher.core.runtime.javaexecutable.checker;

import com.launcher.core.runtime.javaexecutable.exception.JavaExecutableNotReadyException;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJavaExecutableReadinessCheckerTest {
    private final DefaultJavaExecutableReadinessChecker checker =
            new DefaultJavaExecutableReadinessChecker();

    @Test
    void should_reject_non_explicit_path_java_executable_reference() {
        //given
        JavaExecutableReference reference = JavaExecutableReference.commandName("java");

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(reference)
        );

        assertTrue(exception.getMessage().contains("Java executable reference is not an explicit path: " + reference.value()));
    }

    @Test
    void should_reject_directory_as_java_executable_reference(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path directory = tempDir.resolve("directory");
        JavaExecutableReference reference = JavaExecutableReference.explicitPath(directory.toString());
        Files.createDirectory(reference.path());

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(reference)
        );

        assertTrue(exception.getMessage().contains("Java executable is not a file: " + reference.value()));
    }

    @Test
    void should_reject_missing_java_executable_reference(@TempDir Path tempDir) {
        //given
        Path path = tempDir.resolve("test-path");
        JavaExecutableReference reference = JavaExecutableReference.explicitPath(path.toString());

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(reference)
        );

        assertTrue(exception.getMessage().contains("Java executable does not exist: " + reference.value()));
    }

    @Test
    void should_accept_existing_regular_file(@TempDir Path tempDir) throws IOException {
        //given
        Path path = tempDir.resolve("test-path");
        JavaExecutableReference reference = JavaExecutableReference.explicitPath(path.toString());

        Files.createFile(reference.path());

        //when & then
        assertDoesNotThrow(() -> checker.checkReady(reference));
    }

    @Test
    void should_reject_null_java_executable_reference() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checker.checkReady(null)
        );

        assertEquals(
                "javaExecutableReference",
                exception.getMessage()
        );
    }

}
