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

    static JavaProcessDiagnostic processStartFailed() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.PROCESS_START_FAILED,
                "Java process could not be started"
        );
    }

    static JavaProcessDiagnostic processInterrupted() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.PROCESS_INTERRUPTED,
                "Java process was interrupted"
        );
    }

    static JavaProcessDiagnostic processTimedOut() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.PROCESS_TIMED_OUT,
                "Java process timed out"
        );
    }

    static JavaProcessDiagnostic outputLimitExceeded() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.OUTPUT_LIMIT_EXCEEDED,
                "Java process output exceeded the byte limit"
        );
    }

    static JavaProcessDiagnostic outputReadFailed() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.OUTPUT_READ_FAILED,
                "Java process output could not be read"
        );
    }

    static JavaProcessDiagnostic processCleanupFailed() {
        return new JavaProcessDiagnostic(
                JavaProcessFailureReason.PROCESS_CLEANUP_FAILED,
                "Java process cleanup did not complete"
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
