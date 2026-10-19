package com.launcher.app.runtime.detection;

import java.time.Duration;
import java.util.Objects;

record JavaRuntimeVersionProbeLimits(
        Duration executionTimeout,
        int maxOutputBytes,
        Duration cleanupTimeout
) {

    static JavaRuntimeVersionProbeLimits defaults() {
        return new JavaRuntimeVersionProbeLimits(
                Duration.ofSeconds(10),
                64 * 1024,
                Duration.ofSeconds(5)
        );
    }

    JavaRuntimeVersionProbeLimits {
        validateTimeout(executionTimeout, "executionTimeout");
        validateTimeout(cleanupTimeout, "cleanupTimeout");

        if (maxOutputBytes <= 0 || maxOutputBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "maxOutputBytes must be positive and less than Integer.MAX_VALUE"
            );
        }
    }

    private static void validateTimeout(Duration timeout, String name) {
        Objects.requireNonNull(timeout, name);

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException(
                    name + " must be positive"
            );
        }

        try {
            timeout.toNanos();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    name + " is too large",
                    e
            );
        }
    }
}
