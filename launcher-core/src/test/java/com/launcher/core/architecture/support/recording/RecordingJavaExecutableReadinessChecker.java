package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.JavaExecutableReadinessChecker;
import com.launcher.model.runtime.JavaExecutableReference;

public final class RecordingJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {
    private JavaExecutableReference javaExecutableReference;
    private boolean notValid = false;

    @Override
    public void checkReady(JavaExecutableReference javaExecutableReference) {
        if (notValid) {
            throw new IllegalStateException("Java executable is not valid");
        }

        this.javaExecutableReference = javaExecutableReference;
    }

    public void setNotValid() {
        notValid = true;
    }

    public JavaExecutableReference getJavaExecutableReference() {
        return javaExecutableReference;
    }
}
