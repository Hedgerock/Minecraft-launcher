package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ProcessJavaRuntimeVersionCommandRunner implements JavaRuntimeVersionCommandRunner {
    private final JavaRuntimeVersionProcessStarter processStarter;

    ProcessJavaRuntimeVersionCommandRunner() {
        this(new ProcessBuilderJavaRuntimeVersionProcessStarter());
    }

    ProcessJavaRuntimeVersionCommandRunner(
            JavaRuntimeVersionProcessStarter processStarter
    ) {
        this.processStarter = Objects.requireNonNull(processStarter, "processStarter");
    }

    @Override
    public JavaRuntimeVersionCommandResult run(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        try {
            Process process = processStarter.start(javaExecutableReference);

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            int exitCode = process.waitFor();

            return new JavaRuntimeVersionCommandResult(exitCode, output);
        } catch (IOException e) {
            throw new JavaRuntimeVersionDetectionException(
                    JavaProcessDiagnostic.processStartFailed().message()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JavaRuntimeVersionDetectionException(
                    JavaProcessDiagnostic.processInterrupted().message()
            );
        }
    }
}
