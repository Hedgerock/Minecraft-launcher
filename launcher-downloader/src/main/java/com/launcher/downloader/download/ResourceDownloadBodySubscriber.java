package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class ResourceDownloadBodySubscriber implements HttpResponse.BodySubscriber<Void> {

    private final String url;
    private final Path targetPath;
    private final long expectedSize;
    private final OutputStream output;

    private final byte[] buffer = new byte[8192];
    private final CompletableFuture<Void> body = new CompletableFuture<>();

    private Flow.Subscription subscription;
    private long writtenBytes;
    private boolean terminated;
    private boolean outputClosed;

    ResourceDownloadBodySubscriber(
            String url,
            Path targetPath,
            long expectedSize,
            OutputStream output
    ) {
        this.url = Objects.requireNonNull(url, "url");
        this.targetPath = Objects.requireNonNull(targetPath, "targetPath");

        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize must not be negative");
        }

        this.expectedSize = expectedSize;
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public CompletionStage<Void> getBody() {
        return body;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");

        if (this.subscription != null || terminated) {
            subscription.cancel();
            return;
        }

        this.subscription = subscription;
        requestNext();
    }

    @Override
    public void onNext(List<ByteBuffer> items) {
        Objects.requireNonNull(items, "items");

        if (terminated) {
            return;
        }

        try {
            long remainingBytes = expectedSize - writtenBytes;

            for (ByteBuffer item : items) {
                if (item.remaining() > remainingBytes) {
                    fail(DownloadException.sizeMismatch(url, targetPath));
                    return;
                }

                remainingBytes -= item.remaining();
            }

            for (ByteBuffer item : items) {
                while (item.hasRemaining()) {
                    int count = Math.min(item.remaining(), buffer.length);
                    item.get(buffer, 0, count);
                    output.write(buffer, 0, count);
                    writtenBytes += count;
                }
            }
        } catch (IOException | RuntimeException failure) {
            fail(failure);
            return;
        }

        requestNext();
    }

    @Override
    public void onError(Throwable failure) {
        fail(Objects.requireNonNull(failure, "failure"));
    }

    @Override
    public void onComplete() {
        if (terminated) {
            return;
        }

        if (writtenBytes != expectedSize) {
            fail(DownloadException.sizeMismatch(url, targetPath));
            return;
        }

        try {
            closeOutput();
        } catch (IOException | RuntimeException failure) {
            fail(failure);
            return;
        }

        terminated = true;
        body.complete(null);
    }

    private void requestNext() {
        try {
            subscription.request(1);
        } catch (RuntimeException failure) {
            fail(failure);
        }
    }

    private void fail(Throwable failure) {
        if (terminated) {
            return;
        }

        terminated = true;

        if (subscription != null) {
            try {
                subscription.cancel();
            } catch (RuntimeException cancellationFailure) {
                addSuppressed(failure, cancellationFailure);
            }
        }

        try {
            closeOutput();
        } catch (IOException | RuntimeException closeFailure) {
            addSuppressed(failure, closeFailure);
        }

        body.completeExceptionally(failure);
    }

    private void closeOutput() throws IOException {
        if (outputClosed) {
            return;
        }

        outputClosed = true;
        output.close();
    }

    private static void addSuppressed(
            Throwable failure,
            Throwable secondaryFailure
    ) {
        if (failure != secondaryFailure) {
            failure.addSuppressed(secondaryFailure);
        }
    }
}
