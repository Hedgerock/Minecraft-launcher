package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.compatibility.JavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

public final class RecordingJavaRuntimeCompatibilityChecker implements JavaRuntimeCompatibilityChecker {
    private JavaRuntimeCompatibilityRequest request;
    private RuntimeException exception;

    @Override
    public void checkCompatible(JavaRuntimeCompatibilityRequest request) {
        this.request = request;

        if (exception != null) {
            throw exception;
        }
    }

    public JavaRuntimeCompatibilityRequest getRequest() {
        return request;
    }

    public void failWith(RuntimeException exception) {
        this.exception = exception;
    }
}
