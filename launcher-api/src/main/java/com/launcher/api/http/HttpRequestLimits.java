package com.launcher.api.http;

import java.time.Duration;
import java.util.Objects;

public record HttpRequestLimits(
        Duration timeout,
        int maxResponseBytes
) {

    public HttpRequestLimits {
        Objects.requireNonNull(timeout, "timeout");

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        try {
            timeout.toNanos();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("timeout is too large", e);
        }

        if (maxResponseBytes <= 0) {
            throw new IllegalArgumentException("maxResponseBytes must be positive");
        }
    }
}
