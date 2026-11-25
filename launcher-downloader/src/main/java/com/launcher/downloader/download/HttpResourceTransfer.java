package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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
    private static final int MAX_TECHNICAL_REQUESTS = 20;
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

        if (httpClient.followRedirects() != HttpClient.Redirect.NEVER) {
            throw new IllegalArgumentException(
                    "HTTP client must not follow redirects automatically"
            );
        }
    }

    void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        transfer(url, temporaryFile, targetPath, expectedSize, limits, false);
    }

    void transferTechnical(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        transfer(url, temporaryFile, targetPath, expectedSize, limits, true);
    }

    private void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits,
            boolean technical
    ) throws IOException {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(temporaryFile, "temporaryFile");
        Objects.requireNonNull(targetPath, "targetPath");
        Objects.requireNonNull(limits, "limits");

        limits.validateExpectedSize(expectedSize);

        long timeoutNanos = limits.timeout().toNanos();
        long startedAt = System.nanoTime();
        URI uri = URI.create(url);
        boolean followRedirects = technical && HttpURLConnection.getFollowRedirects();
        int requests = 0;

        try (
                OutputStream output = Files.newOutputStream(
                        temporaryFile,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            while (true) {
                HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
                requests++;

                try {
                    execute(
                            request,
                            response -> selectSubscriber(
                                    response,
                                    request.uri(),
                                    technical,
                                    followRedirects,
                                    url,
                                    targetPath,
                                    expectedSize,
                                    output
                            ),
                            startedAt,
                            timeoutNanos
                    );
                    return;
                } catch (RedirectResponse redirect) {
                    if (requests >= MAX_TECHNICAL_REQUESTS) {
                        throw new IOException("Too many resource HTTP redirects", redirect);
                    }

                    uri = redirect.location;
                }
            }
        } catch (TimeoutException failure) {
            throw new IOException("Resource HTTP transfer timed out", failure);
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IOException("Resource HTTP transfer interrupted", failure);
        }
    }

    private static HttpResponse.BodySubscriber<Void> selectSubscriber(
            HttpResponse.ResponseInfo response,
            URI requestUri,
            boolean technical,
            boolean followRedirects,
            String url,
            Path targetPath,
            long expectedSize,
            OutputStream output
    ) {
        int status = response.statusCode();

        if (followRedirects && isTechnicalRedirect(status)) {
            String location = response.headers().firstValue("Location").orElse(null);

            if (location != null) {
                try {
                    URI nextUri = requestUri.resolve(location);

                    if (requestUri.getScheme().equalsIgnoreCase(nextUri.getScheme())) {
                        if (nextUri.getHost() == null) {
                            return new RejectedBodySubscriber(
                                    new ProtocolException("Resource HTTP redirect requires a host")
                            );
                        }

                        return new RejectedBodySubscriber(new RedirectResponse(nextUri));
                    }
                } catch (IllegalArgumentException failure) {
                    return new RejectedBodySubscriber(
                            new IOException("Invalid resource HTTP redirect", failure)
                    );
                }
            }
        }

        boolean accepted = technical
                ? status >= 200 && status < 400 && status != 305
                : status == 200;

        if (!accepted) {
            return new RejectedBodySubscriber(
                    new IOException("Unexpected resource HTTP status: " + status)
            );
        }

        return new ResourceDownloadBodySubscriber(url, targetPath, expectedSize, output);
    }

    private void execute(
            HttpRequest request,
            HttpResponse.BodyHandler<Void> handler,
            long startedAt,
            long timeoutNanos
    ) throws IOException, TimeoutException, InterruptedException {
        checkInterrupted();
        remainingNanos(startedAt, timeoutNanos);

        CompletableFuture<HttpResponse<Void>> exchange = httpClient.sendAsync(request, handler);

        try {
            exchange.get(remainingNanos(startedAt, timeoutNanos), TimeUnit.NANOSECONDS);
            checkInterrupted();
            remainingNanos(startedAt, timeoutNanos);
        } catch (TimeoutException | InterruptedException failure) {
            cancelExchange(exchange, failure);
            throw failure;
        } catch (CancellationException failure) {
            throw new IOException("Resource HTTP transfer cancelled", failure);
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

            throw new IOException("Resource HTTP transfer failed", cause);
        }
    }

    private static long remainingNanos(long startedAt, long timeoutNanos)
            throws TimeoutException {
        long remaining = timeoutNanos - (System.nanoTime() - startedAt);

        if (remaining <= 0) {
            throw new TimeoutException("Resource HTTP deadline exceeded");
        }

        return remaining;
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("Resource HTTP transfer interrupted");
        }
    }

    private static boolean isTechnicalRedirect(int status) {
        return switch (status) {
            case 300, 301, 302, 303, 307 -> true;
            default -> false;
        };
    }

    private static void cancelExchange(CompletableFuture<?> exchange, Throwable failure) {
        try {
            exchange.cancel(true);
        } catch (RuntimeException cancellationFailure) {
            if (failure != cancellationFailure) {
                failure.addSuppressed(cancellationFailure);
            }
        }
    }

    private static final class RedirectResponse extends IOException {
        private final URI location;

        private RedirectResponse(URI location) {
            super("Resource HTTP redirect");
            this.location = location;
        }
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
