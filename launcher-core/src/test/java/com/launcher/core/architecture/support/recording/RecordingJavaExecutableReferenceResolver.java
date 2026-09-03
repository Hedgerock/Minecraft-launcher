package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.javaexecutable.resolver.JavaExecutableReferenceResolver;
import com.launcher.model.runtime.JavaExecutableReference;

public final class RecordingJavaExecutableReferenceResolver implements JavaExecutableReferenceResolver {
    private String javaExecutable;

    @Override
    public JavaExecutableReference resolve(String javaExecutable) {
        this.javaExecutable = javaExecutable;
        return JavaExecutableReference.commandName(javaExecutable);
    }

    public String getJavaExecutable() {
        return javaExecutable;
    }
}
