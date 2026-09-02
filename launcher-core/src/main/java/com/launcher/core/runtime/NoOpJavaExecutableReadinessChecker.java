package com.launcher.core.runtime;

import java.nio.file.Path;
import java.util.Objects;

public final class NoOpJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {

    @Override
    public void checkReady(Path javaExecutable) {
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        // No filesystem checks until explicit Java path resolution is introduced
    }
}
