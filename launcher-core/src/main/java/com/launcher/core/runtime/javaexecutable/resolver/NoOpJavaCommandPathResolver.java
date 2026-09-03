package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class NoOpJavaCommandPathResolver implements JavaCommandPathResolver {

    @Override
    public JavaExecutableReference resolve(JavaExecutableReference javaExecutableReference) {
        return Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");
    }
}
