package com.launcher.testing.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class PausedHttpResponse {

    private final int status;
    private final byte[] prefix;
    private final byte[] suffix;
    private final long releaseTimeoutNanos;

    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch released = new CountDownLatch(1);
    private final CountDownLatch finished = new CountDownLatch(1);

    PausedHttpResponse(
            int status,
            byte[] prefix,
            byte[] suffix,
            Duration releaseTimeout
    ) {
        this.status = status;
        this.prefix = Objects.requireNonNull(prefix, "prefix").clone();
        this.suffix = Objects.requireNonNull(suffix, "suffix").clone();
        this.releaseTimeoutNanos = Objects.requireNonNull(releaseTimeout, "releaseTimeout").toNanos();

        if (releaseTimeoutNanos <= 0) {
            throw new IllegalArgumentException("ReleaseTimeout must be positive");
        }
    }

    public boolean awaitStarted(Duration timeout) throws InterruptedException {
        return started.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public boolean awaitFinished(Duration timeout) throws InterruptedException {
        return finished.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void release() {
        released.countDown();
    }

    void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            exchange.sendResponseHeaders(status, 0);

            OutputStream body = exchange.getResponseBody();

            if (prefix.length > 0) {
                body.write(prefix);
            }

            body.flush();
            started.countDown();

            awaitRelease();

            if (suffix.length > 0) {
                body.write(suffix);
            }
        } finally {
            finished.countDown();
        }
    }

    private void awaitRelease() throws IOException {
        try {
            if (!released.await(releaseTimeoutNanos, TimeUnit.NANOSECONDS)) {
                throw new IOException("Test did not release the HTTP response");
            }
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IOException("Test HTTP response handler interrupted", failure);
        }
    }
}
