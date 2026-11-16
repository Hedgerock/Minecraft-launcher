package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedBodySubscriberTest {

    @Test
    void should_preserve_cause_when_body_transfer_fails() {
        //given
        BoundedBodySubscriber<byte[]> subscriber = new BoundedBodySubscriber<>(
                HttpResponse.BodySubscribers.ofByteArray(),
                10
        );

        RecordingSubscription subscription = new RecordingSubscription();

        IOException transferFailure = new IOException("Connection reset");

        //when
        subscriber.onSubscribe(subscription);

        subscriber.onNext(
                List.of(ByteBuffer.wrap(new byte[4]))
        );

        subscriber.onError(transferFailure);

        //then
        CompletableFuture<byte[]> body = subscriber.getBody().toCompletableFuture();
        assertTrue(body.isDone(), "Body result must complete after a transfer failure");

        CompletionException exception = assertThrows(
                CompletionException.class,
                body::join
        );

        assertSame(transferFailure, exception.getCause());
    }

    @Test
    void should_cancel_subscription_when_total_body_size_exceeds_limit() {
        //given
        BoundedBodySubscriber<byte[]> subscriber = new BoundedBodySubscriber<>(
                HttpResponse.BodySubscribers.ofByteArray(),
                4
        );

        RecordingSubscription subscription = new RecordingSubscription();

        byte[] firstChunk = new byte[2];
        byte[] secondChunk = new byte[2];
        byte[] thirdChunk = new byte[1];

        //when
        subscriber.onSubscribe(subscription);

        subscriber.onNext(List.of(ByteBuffer.wrap(firstChunk)));
        subscriber.onNext(List.of(ByteBuffer.wrap(secondChunk), ByteBuffer.wrap(thirdChunk)));

        //then
        assertTrue(subscription.isCancelled());
        CompletableFuture<byte[]> body = subscriber.getBody().toCompletableFuture();
        assertTrue(body.isDone(), "Body result must complete when the size limit is exceeded");

        CompletionException exception = assertThrows(
                CompletionException.class,
                body::join
        );

        assertInstanceOf(
                HttpRequestException.class,
                exception.getCause()
        );
    }

    @Test
    void should_accept_body_when_total_size_matches_limit() {
        //given
        BoundedBodySubscriber<byte[]> subscriber = new BoundedBodySubscriber<>(
                HttpResponse.BodySubscribers.ofByteArray(),
                4
        );

        RecordingSubscription subscription = new RecordingSubscription();

        byte[] firstChunk = {1, 2};
        byte[] secondChunk = {3, 4};

        //when
        subscriber.onSubscribe(subscription);

        subscriber.onNext(List.of(ByteBuffer.wrap(firstChunk)));
        subscriber.onNext(List.of(ByteBuffer.wrap(secondChunk)));

        subscriber.onComplete();

        //then
        CompletableFuture<byte[]> body = subscriber.getBody().toCompletableFuture();
        assertTrue(body.isDone(), "Body result must complete after onComplete");

        assertArrayEquals(
                new byte[]{1, 2, 3, 4},
                body.join()
        );

        assertFalse(subscription.isCancelled());
    }
}
