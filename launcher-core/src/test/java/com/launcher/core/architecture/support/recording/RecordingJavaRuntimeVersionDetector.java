package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.detection.JavaRuntimeVersionDetector;
import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaRuntimeVersion;

public final class RecordingJavaRuntimeVersionDetector implements JavaRuntimeVersionDetector {
    private JavaRuntimeVersionDetectionRequest request;
    private final JavaRuntimeVersion javaRuntimeVersion = new JavaRuntimeVersion(18);

    @Override
    public JavaRuntimeVersion detect(JavaRuntimeVersionDetectionRequest request) {
        this.request = request;
        return javaRuntimeVersion;
    }

    public JavaRuntimeVersionDetectionRequest getRequest() {
        return request;
    }

    public JavaRuntimeVersion getJavaRuntimeVersion() {
        return javaRuntimeVersion;
    }
}
