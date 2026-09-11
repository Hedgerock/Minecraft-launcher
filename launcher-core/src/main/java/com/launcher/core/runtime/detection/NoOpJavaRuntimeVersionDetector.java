package com.launcher.core.runtime.detection;

import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaRuntimeVersion;

import java.util.Objects;

public final class NoOpJavaRuntimeVersionDetector implements JavaRuntimeVersionDetector {

    @Override
    public JavaRuntimeVersion detect(JavaRuntimeVersionDetectionRequest request) {
        Objects.requireNonNull(request, "request");

        return new JavaRuntimeVersion(17);
    }
}
