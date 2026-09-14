package com.launcher.app.runtime.detection;

import java.util.Objects;

record JavaProcessDiagnostic(
        JavaProcessFailureReason reason,
        String message
) {
    static JavaProcessDiagnostic emptyOutput() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.EMPTY_OUTPUT,
                "Java process returned empty output"
        );
    }

    static JavaProcessDiagnostic outputParsingFailed() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.OUTPUT_PARSING_FAILED,
                "Java process output cannot be parsed as Java runtime version"
        );
    }

    JavaProcessDiagnostic {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
