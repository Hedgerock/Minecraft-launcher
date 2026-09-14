package com.launcher.app.runtime.detection;

import java.util.Objects;

record JavaProcessDiagnostic(
        JavaProcessFailureReason reason,
        String message
) {
    JavaProcessDiagnostic {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
