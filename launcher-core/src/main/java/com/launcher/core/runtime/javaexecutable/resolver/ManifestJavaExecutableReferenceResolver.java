package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class ManifestJavaExecutableReferenceResolver implements JavaExecutableReferenceResolver {

    @Override
    public JavaExecutableReference resolve(String javaExecutable) {
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        return JavaExecutableReference.commandName(javaExecutable);
    }
}
