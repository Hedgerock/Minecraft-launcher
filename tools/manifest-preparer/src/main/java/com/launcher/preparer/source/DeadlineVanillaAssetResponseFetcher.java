package com.launcher.preparer.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class DeadlineVanillaAssetResponseFetcher implements VanillaAssetResponseFetcher {

    private final HttpClient client;
    private final long timeoutNanos;

    DeadlineVanillaAssetResponseFetcher(
            HttpClient client,
            Duration requestTimeout
    ) {
        this.client = Objects.requireNonNull(client, "client");
        Objects.requireNonNull(requestTimeout, "requestTimeout");

        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }

        try {
            this.timeoutNanos = requestTimeout.toNanos();
        } catch (ArithmeticException cause) {
            throw new IllegalArgumentException("requestTimeout is too large", cause);
        }
    }

    @Override
    public Response fetch(URI uri) throws IOException, InterruptedException {
        checkInterrupted();

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        long startedAtNanos = System.nanoTime();
        DeadlineVanillaAssetBodySubscriber body = new DeadlineVanillaAssetBodySubscriber(startedAtNanos, timeoutNanos);
        CompletableFuture<HttpResponse<InputStream>> exchange =
                client.sendAsync(request, ignored -> body);

        try {
            HttpResponse<InputStream> response = awaitHeaders(exchange, startedAtNanos);
            return new Response(response.statusCode(), response.body());
        } catch (IOException | InterruptedException | RuntimeException | Error failure) {
            abort(body, exchange, failure);
            throw failure;
        }
    }

    private HttpResponse<InputStream> awaitHeaders(
            CompletableFuture<HttpResponse<InputStream>> exchange,
            long startedAtNanos
    ) throws IOException, InterruptedException {
        try {
            HttpResponse<InputStream> response = exchange.get(
                    remainingNanos(startedAtNanos),
                    TimeUnit.NANOSECONDS
            );

            checkInterrupted();
            remainingNanos(startedAtNanos);
            return response;
        } catch (TimeoutException cause) {
            HttpTimeoutException failure = headerTimeout();
            failure.initCause(cause);
            throw failure;
        } catch (CancellationException cause) {
            throw new IOException("Vanilla asset HTTP request was cancelled", cause);
        } catch (ExecutionException cause) {
            Throwable failure = cause.getCause();

            if (failure instanceof IOException ioFailure) {
                throw ioFailure;
            }

            if (failure instanceof Error error) {
                throw error;
            }

            throw new IOException("Vanilla asset HTTP request failed", failure);
        }
    }

    private long remainingNanos(long startedAtNanos) throws HttpTimeoutException {
        long remaining = timeoutNanos - (System.nanoTime() - startedAtNanos);

        if (remaining <= 0) {
            throw headerTimeout();
        }

        return remaining;
    }

    private static HttpTimeoutException headerTimeout() {
        return new HttpTimeoutException("Vanilla asset HTTP deadline exceeded before headers");
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("Vanilla asset HTTP request interrupted");
        }
    }

    private static void abort(
            DeadlineVanillaAssetBodySubscriber body,
            CompletableFuture<?> exchange,
            Throwable primaryFailure
    ) {
        try {
            body.close();
        } catch (RuntimeException cleanupFailure) {
            if (cleanupFailure != primaryFailure) {
                primaryFailure.addSuppressed(cleanupFailure);
            }
        }

        try {
            exchange.cancel(true);
        } catch (RuntimeException cleanupFailure) {
            if (cleanupFailure != primaryFailure) {
                primaryFailure.addSuppressed(cleanupFailure);
            }
        }
    }
}
