package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ProcessJavaRuntimeVersionCommandRunner implements JavaRuntimeVersionCommandRunner {

    @Override
    public JavaRuntimeVersionCommandResult run(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        ProcessBuilder processBuilder = new ProcessBuilder(
                javaExecutableReference.value(),
                "-version"
        );

        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            int exitCode = process.waitFor();

            return new JavaRuntimeVersionCommandResult(exitCode, output);
        } catch (IOException e) {
            throw new JavaRuntimeVersionDetectionException("Failed to start Java version process");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JavaRuntimeVersionDetectionException("Java version process was interrupted");
        }
    }
}
