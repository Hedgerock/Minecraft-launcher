package com.launcher.core.runtime;

import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class NoOpJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {

    @Override
    public void checkReady(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        // No filesystem checks until explicit Java path resolution is introduced
    }
}
