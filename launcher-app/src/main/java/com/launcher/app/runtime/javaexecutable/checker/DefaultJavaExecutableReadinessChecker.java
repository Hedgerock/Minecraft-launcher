package com.launcher.app.runtime.javaexecutable.checker;

import com.launcher.core.runtime.javaexecutable.checker.JavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.exception.JavaExecutableNotReadyException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.model.runtime.JavaExecutableReference;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public final class DefaultJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {

    @Override
    public void checkReady(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        if (!javaExecutableReference.isExplicitPath()) {
            throw new JavaExecutableNotReadyException(
                    JavaRuntimeFailureReason.NON_EXPLICIT_JAVA_EXECUTABLE_REFERENCE,
                    "Java executable reference is not an explicit path: " + javaExecutableReference.value()
            );
        }

        Path javaExecutable = getJavaExecutablePath(javaExecutableReference);

        if (!Files.exists(javaExecutable)) {
            throw new JavaExecutableNotReadyException(
                    JavaRuntimeFailureReason.JAVA_EXECUTABLE_NOT_FOUND,
                    "Java executable does not exist: " + javaExecutable
            );
        }

        if (!Files.isRegularFile(javaExecutable)) {
            throw new JavaExecutableNotReadyException(
                    JavaRuntimeFailureReason.JAVA_EXECUTABLE_NOT_REGULAR_FILE,
                    "Java executable is not a file: " + javaExecutable
            );
        }
    }

    private Path getJavaExecutablePath(JavaExecutableReference javaExecutableReference) {
        try {
            return javaExecutableReference.path();
        } catch (InvalidPathException e) {
            throw new JavaExecutableNotReadyException(
                    JavaRuntimeFailureReason.INVALID_EXPLICIT_PATH,
                    "Java executable path is invalid: " + javaExecutableReference.value()
            );
        }
    }
}
