package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.JavaExecutableReadinessChecker;

import java.nio.file.Path;

public final class RecordingJavaExecutableReadinessChecker implements JavaExecutableReadinessChecker {
    private Path javaExecutable;
    private boolean notValid = false;

    @Override
    public void checkReady(Path javaExecutable) {
        if (notValid) {
            throw new IllegalStateException("Java executable is not valid");
        }

        this.javaExecutable = javaExecutable;
    }

    public void setNotValid() {
        notValid = true;
    }

    public Path getJavaExecutable() {
        return javaExecutable;
    }
}
