package com.launcher.downloader.download;

import java.time.Duration;
import java.util.Objects;

record ResourceDownloadLimits(
        Duration timeout,
        long maxResourceBytes
) {

    ResourceDownloadLimits {
        Objects.requireNonNull(timeout, "timeout");

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        try {
            timeout.toNanos();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("timeout is too large", e);
        }

        if (maxResourceBytes <= 0) {
            throw new IllegalArgumentException("maxResourceBytes must be positive");
        }
    }

    static ResourceDownloadLimits defaults() {
        return new ResourceDownloadLimits(
                Duration.ofMinutes(10),
                4L * 1024 * 1024 * 1024
        );
    }

    void validateExpectedSize(long expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize must not be negative");
        }

        if (expectedSize > maxResourceBytes) {
            throw new IllegalArgumentException("expectedSize exceeds maxResourceBytes");
        }
    }
}
