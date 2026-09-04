package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class ManifestJavaExecutableReferenceResolver implements JavaExecutableReferenceResolver {

    @Override
    public JavaExecutableReference resolve(String javaExecutable) {
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        if (javaExecutable.isBlank()) {
            throw new IllegalArgumentException(
                    "javaExecutable must not be blank"
            );
        }

        if (containsPathSeparator(javaExecutable)) {
            return JavaExecutableReference.explicitPath(javaExecutable);
        }

        return JavaExecutableReference.commandName(javaExecutable);
    }

    private boolean containsPathSeparator(String javaExecutable) {
        return javaExecutable.contains("/") || javaExecutable.contains("\\");
    }
}
