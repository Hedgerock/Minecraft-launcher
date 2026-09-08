package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.core.runtime.model.JavaRuntimeSelectionRequest;
import com.launcher.model.runtime.JavaExecutableReference;

public final class RecordingJavaRuntimeSelector implements JavaRuntimeSelector {
    private JavaRuntimeSelectionRequest request;

    @Override
    public JavaExecutableReference selectJavaExecutable(JavaRuntimeSelectionRequest request) {
        this.request = request;
        return JavaExecutableReference.commandName("new-java-executable");
    }

    public JavaRuntimeSelectionRequest getJavaRuntimeSelectionRequest() {
        return request;
    }
}
