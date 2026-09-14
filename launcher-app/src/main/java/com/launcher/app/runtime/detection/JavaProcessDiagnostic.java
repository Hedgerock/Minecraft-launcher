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

    JavaProcessDiagnostic {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
