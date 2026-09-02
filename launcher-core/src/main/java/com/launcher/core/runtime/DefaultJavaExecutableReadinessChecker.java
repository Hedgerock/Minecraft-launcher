package com.launcher.core.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class DefaultJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {

    @Override
    public void checkReady(Path javaExecutable) {
        Objects.requireNonNull(javaExecutable, "javaExecutable");

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
