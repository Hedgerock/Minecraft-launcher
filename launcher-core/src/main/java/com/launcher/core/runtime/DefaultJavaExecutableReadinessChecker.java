package com.launcher.core.runtime;

import com.launcher.model.runtime.JavaExecutableReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class DefaultJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {

    @Override
    public void checkReady(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        if (!javaExecutableReference.isExplicitPath()) {
            throw new JavaExecutableNotReadyException(
                    "Java executable reference is not an explicit path: " + javaExecutableReference.value()
            );
        }

        Path javaExecutable = javaExecutableReference.path();

        if (!Files.exists(javaExecutable)) {
            throw new JavaExecutableNotReadyException(
                    "Java executable does not exist: " + javaExecutable
            );
        }

        if (!Files.isRegularFile(javaExecutable)) {
            throw new JavaExecutableNotReadyException(
                    "Java executable is not a file: " + javaExecutable
            );
        }
    }
}
