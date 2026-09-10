package com.launcher.core.runtime.compatibility;

import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

public interface JavaRuntimeCompatibilityChecker {

    void checkCompatible(JavaRuntimeCompatibilityRequest request);
}
