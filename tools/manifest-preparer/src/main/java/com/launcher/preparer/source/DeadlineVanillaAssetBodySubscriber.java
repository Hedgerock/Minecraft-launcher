package com.launcher.preparer.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

final class DeadlineVanillaAssetBodySubscriber
        extends InputStream
        implements HttpResponse.BodySubscriber<InputStream>
{

    private final Object monitor = new Object();
    private final Deque<ByteBuffer> pending = new ArrayDeque<>();
    private final CompletionStage<InputStream> body = CompletableFuture.completedStage(this);
    private final long startedAtNanos;
    private final long timeoutNanos;
    private final LongSupplier nanoTime;

    private Flow.Subscription subscription;
    private boolean requestPending;
    private boolean completed;
    private boolean closed;
    private Throwable failure;

    DeadlineVanillaAssetBodySubscriber(long startedAtNanos, long timeoutNanos) {
        this(startedAtNanos, timeoutNanos, System::nanoTime);
    }

    DeadlineVanillaAssetBodySubscriber(
            long startedAtNanos,
            long timeoutNanos,
            LongSupplier nanoTime
    ) {
        if (timeoutNanos <= 0) {
            throw new IllegalArgumentException("timeoutNanos must be positive");
        }

        this.startedAtNanos = startedAtNanos;
        this.timeoutNanos = timeoutNanos;
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    @Override
    public CompletionStage<InputStream> getBody() {
        return body;
    }

    @Override
    public void onSubscribe(Flow.Subscription candidate) {
        synchronized (monitor) {
            if (!isTerminal() && subscription == null) {
                subscription = candidate;
                monitor.notifyAll();
                return;
            }
        }

        candidate.cancel();
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        synchronized (monitor) {
            if (isTerminal()) {
                return;
            }

            for (ByteBuffer buffer : buffers) {
                if (buffer.hasRemaining()) {
                    pending.addLast(buffer.duplicate());
                }
            }

            requestPending = false;
            monitor.notifyAll();
        }
    }

    @Override
    public void onError(Throwable cause) {
        synchronized (monitor) {
            if (isTerminal()) {
                return;
            }

            failure = cause;
            pending.clear();
            subscription = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void onComplete() {
        synchronized (monitor) {
            if (isTerminal()) {
                return;
            }

            if (remainingNanos() <= 0) {
                failure = timeoutFailure();
                pending.clear();
            } else {
                completed = true;
            }

            subscription = null;
            monitor.notifyAll();
        }
    }

    @Override
    public int read() throws IOException {
        byte[] oneByte = new byte[1];
        return read(oneByte, 0, 1) == -1 ? -1 : Byte.toUnsignedInt(oneByte[0]);
    }

    @Override
    public int read(
            byte[] target,
            int offset,
            int length
    ) throws IOException {
        Objects.checkFromIndexSize(offset, length, target.length);
        if (length == 0) {
            return 0;
        }

        try {
            return readWithinDeadline(target, offset, length);
        } catch (HttpTimeoutException cause) {
            closeAfterFailure(cause);
            throw cause;
        } catch (InterruptedException cause) {
            InterruptedIOException interrupted = new InterruptedIOException("Vanilla asset HTTP body read interrupted");
            interrupted.initCause(cause);

            try {
                closeAfterFailure(interrupted);
            } finally {
                Thread.currentThread().interrupt();
            }

            throw interrupted;
        }
    }

    private int readWithinDeadline(
            byte[] target,
            int offset,
            int length
    ) throws IOException, InterruptedException {
        while (true) {
            Flow.Subscription toRequest;

            synchronized (monitor) {
                checkReadable();
                long remaining = remainingNanos();

                if (!completed && remaining <= 0) {
                    throw timeoutFailure();
                }

                if (!pending.isEmpty()) {
                    return readPending(target, offset, length);
                }

                if (completed) {
                    return -1;
                }

                if (subscription == null || requestPending) {
                    TimeUnit.NANOSECONDS.timedWait(monitor, remaining);
                    continue;
                }

                requestPending = true;
                toRequest = subscription;
            }

            toRequest.request(1);
        }
    }

    private void checkReadable() throws IOException, InterruptedException {
        if (failure instanceof IOException ioFailure) {
            throw ioFailure;
        }

        if (failure instanceof Error error) {
            throw error;
        }

        if (failure != null) {
            throw new IOException("Vanilla asset HTTP body failed", failure);
        }

        if (closed) {
            throw new IOException("Vanilla asset HTTP body is closed");
        }

        if (Thread.interrupted()) {
            throw new InterruptedException("Vanilla asset HTTP body read interrupted");
        }
    }

    private int readPending(byte[] target, int offset, int length) {
        ByteBuffer buffer = pending.getFirst();
        int count = Math.min(length, buffer.remaining());
        buffer.get(target, offset, count);

        if (!buffer.hasRemaining()) {
            pending.removeFirst();
        }

        return count;
    }

    @Override
    public void close() {
        Flow.Subscription toCancel;

        synchronized (monitor) {
            if (closed) {
                return;
            }

            closed = true;
            pending.clear();
            toCancel = subscription;
            subscription = null;
            monitor.notifyAll();
        }

        if (toCancel != null) {
            toCancel.cancel();
        }
    }

    private void closeAfterFailure(IOException primaryFailure) {
        try {
            close();
        } catch (RuntimeException cancellationFailure) {
            primaryFailure.addSuppressed(cancellationFailure);
        }
    }

    private boolean isTerminal() {
        return closed || completed || failure != null;
    }

    private long remainingNanos() {
        return timeoutNanos - (nanoTime.getAsLong() - startedAtNanos);
    }

    private static HttpTimeoutException timeoutFailure() {
        return new HttpTimeoutException("Vanilla asset HTTP body deadline exceeded");
    }
}
