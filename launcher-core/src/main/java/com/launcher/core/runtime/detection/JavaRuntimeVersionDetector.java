package com.launcher.core.runtime.detection;

import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaRuntimeVersion;

public interface JavaRuntimeVersionDetector {

    JavaRuntimeVersion detect(JavaRuntimeVersionDetectionRequest request);
}
