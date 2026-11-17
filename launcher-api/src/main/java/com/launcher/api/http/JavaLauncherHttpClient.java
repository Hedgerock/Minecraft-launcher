package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaLauncherHttpClient implements LauncherHttpClient {
    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(5);

    private final HttpClient httpClient;

    public JavaLauncherHttpClient() {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(CONNECT_TIMEOUT)
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build()
        );
    }

    JavaLauncherHttpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");

        if (httpClient.followRedirects() != HttpClient.Redirect.NEVER) {
            throw new IllegalArgumentException("HTTP redirects must be disabled");
        }
    }

    @Override
    public String get(URI uri, HttpRequestLimits limits) {
        return execute(uri, limits, BodyHandlers.ofString());
    }

    @Override
    public byte[] getBytes(URI uri, HttpRequestLimits limits) {
        return execute(uri, limits, BodyHandlers.ofByteArray());
    }

    private <T> T execute(
            URI uri,
            HttpRequestLimits limits,
            BodyHandler<T> downstream
    ) {
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(limits, "limits");

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        long timeoutNanos = limits.timeout().toNanos();
        long startedAt = System.nanoTime();

        CompletableFuture<HttpResponse<T>> exchange = httpClient.sendAsync(
                request,
                responseInfo -> {
                    int status = responseInfo.statusCode();

                    if (status < 200 || status >= 300) {
                        return new RejectedBodySubscriber<>(
                                new HttpRequestException(
                                        "HTTP GET failed with status code: " + status
                                )
                        );
                    }

                    return new BoundedBodySubscriber<>(
                            downstream.apply(responseInfo),
                            limits.maxResponseBytes()
                    );
                }
        );

        try {
            long remainingNanos = timeoutNanos - (System.nanoTime() - startedAt);

            if (remainingNanos <= 0) {
                throw new TimeoutException("HTTP request deadline exceeded");
            }

            HttpResponse<T> response = exchange.get(remainingNanos, TimeUnit.NANOSECONDS);

            if (System.nanoTime() - startedAt >= timeoutNanos) {
                throw new TimeoutException("HTTP request deadline exceeded");
            }

            return response.body();
        } catch (TimeoutException e) {
            throw cancelExchange(exchange, new HttpRequestException("HTTP GET timed out", e));
        } catch (InterruptedException e) {
            try {
                throw cancelExchange(exchange, new HttpRequestException("HTTP GET interrupted", e));
            } finally {
                Thread.currentThread().interrupt();
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof HttpRequestException httpFailure) {
                throw httpFailure;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw new HttpRequestException("HTTP GET failed", cause);
        }
    }

    private static HttpRequestException cancelExchange(
            CompletableFuture<?> exchange,
            HttpRequestException failure
    ) {
        try {
            exchange.cancel(true);
        } catch (RuntimeException cancellationFailure) {
            if (cancellationFailure != failure) {
                failure.addSuppressed(cancellationFailure);
            }
        }

        return failure;
    }

    private static final class RejectedBodySubscriber<T> implements BodySubscriber<T> {
        private final CompletionStage<T> body;

        private RejectedBodySubscriber(HttpRequestException failure) {
            this.body = CompletableFuture.failedStage(failure);
        }

        @Override
        public CompletionStage<T> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.cancel();
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            // No body data is requested.
        }

        @Override
        public void onError(Throwable throwable) {
            // The HTTP status is already the reported failure.
        }

        @Override
        public void onComplete() {
            // An empty body does not change the rejected status.
        }
    }
}
