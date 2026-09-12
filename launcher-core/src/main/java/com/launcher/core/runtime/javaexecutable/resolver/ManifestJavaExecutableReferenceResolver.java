package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.core.runtime.javaexecutable.exception.JavaExecutableReferenceResolutionException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class ManifestJavaExecutableReferenceResolver implements JavaExecutableReferenceResolver {

    @Override
    public JavaExecutableReference resolve(String javaExecutable) {
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        if (javaExecutable.isBlank()) {
            throw new JavaExecutableReferenceResolutionException(
                    JavaRuntimeFailureReason.INVALID_RAW_JAVA_EXECUTABLE_VALUE,
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
