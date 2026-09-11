package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

final class RecordingJavaRuntimeVersionCommandRunner implements JavaRuntimeVersionCommandRunner {
    private JavaExecutableReference javaExecutableReference;
    private JavaRuntimeVersionCommandResult result =
            new JavaRuntimeVersionCommandResult(
                    0,
                    "openjdk version \"21.0.8\" 2025-07-15"
            );

    @Override
    public JavaRuntimeVersionCommandResult run(JavaExecutableReference javaExecutableReference) {
        this.javaExecutableReference = javaExecutableReference;
        return result;
    }

    void setResult(JavaRuntimeVersionCommandResult result) {
        this.result = result;
    }

    JavaExecutableReference getJavaExecutableReference() {
        return javaExecutableReference;
    }
}
