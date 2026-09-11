package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.compatibility.JavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

public final class RecordingJavaRuntimeCompatibilityChecker implements JavaRuntimeCompatibilityChecker {
    private JavaRuntimeCompatibilityRequest request;

    @Override
    public void checkCompatible(JavaRuntimeCompatibilityRequest request) {
        this.request = request;
    }

    public JavaRuntimeCompatibilityRequest getRequest() {
        return request;
    }
}
