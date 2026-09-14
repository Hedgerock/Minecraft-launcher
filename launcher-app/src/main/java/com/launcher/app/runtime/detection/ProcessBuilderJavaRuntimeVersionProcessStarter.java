package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

import java.io.IOException;

final class ProcessBuilderJavaRuntimeVersionProcessStarter implements JavaRuntimeVersionProcessStarter {

    @Override
    public Process start(JavaExecutableReference javaExecutableReference) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                javaExecutableReference.value(),
                "-version"
        );

        processBuilder.redirectErrorStream(true);

        return processBuilder.start();
    }
}
