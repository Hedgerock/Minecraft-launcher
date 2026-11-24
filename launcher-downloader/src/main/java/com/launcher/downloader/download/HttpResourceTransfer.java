package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class HttpResourceTransfer {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;

    HttpResourceTransfer() {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(CONNECT_TIMEOUT)
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build()
        );
    }

    HttpResourceTransfer(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(temporaryFile, "temporaryFile");
        Objects.requireNonNull(targetPath, "targetPath");
        Objects.requireNonNull(limits, "limits");

        limits.validateExpectedSize(expectedSize);

        long timeoutNanos = limits.timeout().toNanos();
        long startedAt = System.nanoTime();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();

        boolean interrupted = false;

        try (
                OutputStream output = Files.newOutputStream(
                        temporaryFile,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            ResourceDownloadBodySubscriber subscriber =
                    new ResourceDownloadBodySubscriber(
                            url,
                            targetPath,
                            expectedSize,
                            output
                    );

            CompletableFuture<HttpResponse<Void>> exchange =
                    httpClient.sendAsync(
                            request,
                            responseInfo -> {
                                if (responseInfo.statusCode() != 200) {
                                    return new RejectedBodySubscriber(
                                            new IOException(
                                                    "Unexpected resource HTTP status: "
                                                            + responseInfo.statusCode()
                                            )
                                    );
                                }

                                return subscriber;
                            }
                    );

            try {
                long remainingNanos =
                        timeoutNanos - (System.nanoTime() - startedAt);

                if (remainingNanos <= 0) {
                    throw new TimeoutException(
                            "Resource HTTP deadline exceeded"
                    );
                }

                exchange.get(remainingNanos, TimeUnit.NANOSECONDS);

                if (System.nanoTime() - startedAt >= timeoutNanos) {
                    throw new TimeoutException(
                            "Resource HTTP deadline exceeded"
                    );
                }
            } catch (TimeoutException failure) {
                throw cancelExchange(
                        exchange,
                        new IOException(
                                "Resource HTTP transfer timed out",
                                failure
                        )
                );
            } catch (InterruptedException failure) {
                interrupted = true;

                throw cancelExchange(
                        exchange,
                        new IOException(
                                "Resource HTTP transfer interrupted",
                                failure
                        )
                );
            } catch (CancellationException failure) {
                throw new IOException(
                        "Resource HTTP transfer cancelled",
                        failure
                );
            } catch (ExecutionException failure) {
                Throwable cause = failure.getCause();

                if (cause instanceof DownloadException downloadFailure) {
                    throw downloadFailure;
                }

                if (cause instanceof IOException ioFailure) {
                    throw ioFailure;
                }

                if (cause instanceof Error error) {
                    throw error;
                }

                throw new IOException(
                        "Resource HTTP transfer failed",
                        cause
                );
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static IOException cancelExchange(
            CompletableFuture<?> exchange,
            IOException failure
    ) {
        try {
            exchange.cancel(true);
        } catch (RuntimeException cancellationFailure) {
            failure.addSuppressed(cancellationFailure);
        }

        return failure;
    }

    private static final class RejectedBodySubscriber
            implements HttpResponse.BodySubscriber<Void> {

        private final IOException failure;
        private final CompletionStage<Void> body;

        private RejectedBodySubscriber(IOException failure) {
            this.failure = failure;
            this.body = CompletableFuture.failedStage(failure);
        }

        @Override
        public CompletionStage<Void> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            try {
                subscription.cancel();
            } catch (RuntimeException cancellationFailure) {
                failure.addSuppressed(cancellationFailure);
            }
        }

        @Override
        public void onNext(List<ByteBuffer> items) {
            // No response body data is requested.
        }

        @Override
        public void onError(Throwable failure) {
            // The rejected HTTP status remains the primary failure.
        }

        @Override
        public void onComplete() {
            // An empty body does not change the rejected status.
        }
    }
}
