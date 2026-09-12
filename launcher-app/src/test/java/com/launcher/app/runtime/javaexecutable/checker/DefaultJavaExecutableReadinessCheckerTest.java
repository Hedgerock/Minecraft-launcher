package com.launcher.app.runtime.javaexecutable.checker;

import com.launcher.core.runtime.javaexecutable.exception.JavaExecutableNotReadyException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultJavaExecutableReadinessCheckerTest {
    private final DefaultJavaExecutableReadinessChecker checker =
            new DefaultJavaExecutableReadinessChecker();

    @Test
    void should_reject_invalid_java_executable_path() {
        //given
        JavaExecutableReference reference = JavaExecutableReference.explicitPath("\0");

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(reference)
        );

        assertEquals(
                JavaRuntimeFailureReason.INVALID_EXPLICIT_PATH,
                exception.getReason()
        );

        assertEquals(
                "Java executable path is invalid: " + reference.value(),
                exception.getMessage()
        );
    }

    @Test
    void should_reject_non_explicit_path_java_executable_reference() {
        //given
        JavaExecutableReference reference = JavaExecutableReference.commandName("java");

        //when & then
        JavaExecutableNotReadyException exception = assertThrows(
                JavaExecutableNotReadyException.class,
                () -> checker.checkReady(reference)
        );

        assertEquals(
                JavaRuntimeFailureReason.NON_EXPLICIT_JAVA_EXECUTABLE_REFERENCE,
                exception.getReason()
        );

        assertEquals(
                "Java executable reference is not an explicit path: " + reference.value(),
                exception.getMessage()
        );
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

        assertEquals(
                JavaRuntimeFailureReason.JAVA_EXECUTABLE_NOT_REGULAR_FILE,
                exception.getReason()
        );

        assertEquals(
                "Java executable is not a file: " + reference.value(),
                exception.getMessage()
        );
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

        assertEquals(
                JavaRuntimeFailureReason.JAVA_EXECUTABLE_NOT_FOUND,
                exception.getReason()
        );

        assertEquals(
                "Java executable does not exist: " + reference.value(),
                exception.getMessage()
        );
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
