package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;

import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class BoundedBodySubscriber<T> implements BodySubscriber<T> {
    private final BodySubscriber<T> downstream;
    private final int maxResponseBytes;

    private int remainingBytes;
    private Flow.Subscription subscription;
    private boolean terminated;

    public BoundedBodySubscriber(BodySubscriber<T> downstream, int maxResponseBytes) {
        this.downstream = Objects.requireNonNull(downstream, "downstream");

        if (maxResponseBytes <= 0) {
            throw new IllegalArgumentException("maxResponseBytes must be positive");
        }

        this.maxResponseBytes = maxResponseBytes;
        this.remainingBytes = maxResponseBytes;
    }

    @Override
    public CompletionStage<T> getBody() {
        return downstream.getBody();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (this.subscription != null) {
            subscription.cancel();
            return;
        }

        this.subscription = subscription;
        downstream.onSubscribe(subscription);
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        if (terminated) {
            return;
        }

        for (ByteBuffer buffer : buffers) {
            int bytes = buffer.remaining();

            if (bytes > remainingBytes) {
                try {
                    onError(new HttpRequestException(
                            "HTTP response body exceeds " + maxResponseBytes + " bytes"
                    ));
                } finally {
                    subscription.cancel();
                }

                return;
            }

            remainingBytes -= bytes;
        }

        downstream.onNext(buffers);
    }

    @Override
    public void onError(Throwable failure) {
        if (!terminated) {
            terminated = true;
            downstream.onError(failure);
        }
    }

    @Override
    public void onComplete() {
        if (!terminated) {
            terminated = true;
            downstream.onComplete();
        }
    }
}
