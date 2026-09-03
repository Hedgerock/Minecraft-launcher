package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.javaexecutable.resolver.JavaCommandPathResolver;
import com.launcher.model.runtime.JavaExecutableReference;

public final class RecordingJavaCommandPathResolver implements JavaCommandPathResolver {
    private JavaExecutableReference receivedJavaExecutableReference;
    private final JavaExecutableReference resolvedJavaExecutableReference =
            JavaExecutableReference.explicitPath("java");
    private boolean withError = false;

    @Override
    public JavaExecutableReference resolve(JavaExecutableReference javaExecutableReference) {
        this.receivedJavaExecutableReference = javaExecutableReference;

        if (withError) {
            throw new IllegalStateException("Error resolving java executable");
        }

        return resolvedJavaExecutableReference;
    }

    public JavaExecutableReference getReceivedJavaExecutableReference() {
        return receivedJavaExecutableReference;
    }

    public JavaExecutableReference getResolvedJavaExecutableReference() {
        return resolvedJavaExecutableReference;
    }

    public void setWithError() {
        withError = true;
    }
}
