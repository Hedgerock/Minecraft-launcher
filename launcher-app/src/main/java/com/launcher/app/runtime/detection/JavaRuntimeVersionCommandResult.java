package com.launcher.app.runtime.detection;

import java.util.Objects;

record JavaRuntimeVersionCommandResult(
        int exitCode,
        String output
) {
    boolean isSuccessful() {
        return exitCode == 0;
    }

    JavaProcessDiagnostic toDiagnostic() {
        if (isSuccessful()) {
            throw new IllegalStateException(
                    "Successful Java process result cannot be converted to diagnostic"
            );
        }

        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.NON_ZERO_EXIT_CODE,
                "Java process exited with code " + exitCode
        );
    }

    JavaRuntimeVersionCommandResult {
        Objects.requireNonNull(output, "output");
    }
}
