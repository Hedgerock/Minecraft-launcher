package com.launcher.core.runtime;

import com.launcher.core.runtime.model.JavaRuntimeSelectionRequest;
import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaRuntimeSelector {

    JavaExecutableReference selectJavaExecutable(
            JavaRuntimeSelectionRequest request
    );

}
