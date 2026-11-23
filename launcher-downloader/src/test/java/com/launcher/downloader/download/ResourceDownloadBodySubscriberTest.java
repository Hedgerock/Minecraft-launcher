package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceDownloadBodySubscriberTest {

    private static final String URL = "https://example.com/library.jar";
    private static final Path TARGET_PATH = Path.of("libraries/library.jar");

    @Test
    void should_request_one_batch_at_a_time() {
        //given
        TrackingOutputStream output = new TrackingOutputStream();
        RecordingSubscription subscription = new RecordingSubscription();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(4, output);

        //when
        subscriber.onSubscribe(subscription);

        //then
        assertEquals(1L, subscription.requested);

        //when
        subscriber.onNext(List.of(
                ByteBuffer.wrap(new byte[]{1, 2}),
                ByteBuffer.wrap(new byte[]{3})
        ));

        //then
        assertEquals(2L, subscription.requested);
        assertArrayEquals(new byte[]{1, 2, 3}, output.toByteArray());

        //when
        subscriber.onNext(List.of(
                ByteBuffer.wrap(new byte[]{4})
        ));

        //then
        assertEquals(3L, subscription.requested);

        //when
        subscriber.onComplete();

        //then
        assertEquals(3L, subscription.requested);
        assertTrue(output.closed);
        assertFalse(subscription.cancelled);
    }

    @Test
    void should_complete_after_writing_exact_size_and_closing_output() {
        //given
        TrackingOutputStream output = new TrackingOutputStream();
        RecordingSubscription subscription = new RecordingSubscription();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(4, output);

        CompletableFuture<Void> body = subscriber.getBody()
                .toCompletableFuture();

        subscriber.onSubscribe(subscription);

        //when
        subscriber.onNext(List.of(
                ByteBuffer.wrap(new byte[] {1, 2}),
                ByteBuffer.wrap(new byte[] {3})
        ));

        subscriber.onNext(List.of(
                ByteBuffer.wrap(new byte[] {4})
        ));

        //then
        assertFalse(body.isDone());
        assertFalse(output.closed);

        //when
        subscriber.onComplete();

        //then
        assertTrue(body.isDone());
        body.join();

        assertArrayEquals(
                new byte[] {1, 2, 3, 4},
                output.toByteArray()
        );

        assertTrue(output.closed);
        assertFalse(subscription.cancelled);
    }

    @Test
    void should_accept_empty_body_when_expected_size_is_zero() {
        //given
        TrackingOutputStream output = new TrackingOutputStream();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(0, output);
        subscriber.onSubscribe(new RecordingSubscription());

        //when
        subscriber.onComplete();

        //then
        CompletableFuture<Void> body = subscriber.getBody()
                .toCompletableFuture();

        assertTrue(body.isDone());
        body.join();

        assertEquals(0, output.size());
        assertTrue(output.closed);
    }

    @Test
    void should_reject_excess_batch_before_writing_it() {
        //given
        TrackingOutputStream output = new TrackingOutputStream();
        RecordingSubscription subscription = new RecordingSubscription();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(3, output);

        subscriber.onSubscribe(subscription);
        subscriber.onNext(List.of(
           ByteBuffer.wrap(new byte[]{1})
        ));

        //when
        subscriber.onNext(List.of(
           ByteBuffer.wrap(new byte[]{2}),
           ByteBuffer.wrap(new byte[]{3, 4})
        ));

        //then
        DownloadException failure = assertInstanceOf(
                DownloadException.class,
                getFailure(subscriber)
        );

        assertEquals(DownloadExceptionReason.SIZE_MISMATCH, failure.getReason());
        assertArrayEquals(new byte[]{1}, output.toByteArray());
        assertTrue(output.closed);
        assertTrue(subscription.cancelled);
        assertEquals(2L, subscription.requested);
    }

    @Test
    void should_reject_body_when_it_ends_before_expected_size() {
        //given
        TrackingOutputStream output = new TrackingOutputStream();
        RecordingSubscription subscription = new RecordingSubscription();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(3, output);

        subscriber.onSubscribe(subscription);
        subscriber.onNext(List.of(ByteBuffer.wrap(new byte[]{1, 2})));

        //when
        subscriber.onComplete();

        //then
        DownloadException failure = assertInstanceOf(
                DownloadException.class,
                getFailure(subscriber)
        );

        assertEquals(
                DownloadExceptionReason.SIZE_MISMATCH,
                failure.getReason()
        );

        assertTrue(output.closed);
        assertTrue(subscription.cancelled);
    }

    @Test
    void should_preserve_transfer_failure_and_close_output() {
        //given
        IOException transferFailure = new IOException("Transfer failed");
        TrackingOutputStream output = new TrackingOutputStream();
        RecordingSubscription subscription = new RecordingSubscription();

        ResourceDownloadBodySubscriber subscriber = createSubscriber(3, output);

        subscriber.onSubscribe(subscription);

        //when
        subscriber.onError(transferFailure);

        //then
        assertEquals(1L, subscription.requested);
        assertSame(transferFailure, getFailure(subscriber));
        assertTrue(output.closed);
        assertTrue(subscription.cancelled);
    }

    @Test
    void should_fail_when_output_cannot_be_closed() {
        //given
        IOException closeFailure = new IOException("Close failed");
        TrackingOutputStream output = new TrackingOutputStream();
        output.closeFailure = closeFailure;

        ResourceDownloadBodySubscriber subscriber = createSubscriber(1, output);

        subscriber.onSubscribe(new RecordingSubscription());
        subscriber.onNext(List.of(ByteBuffer.wrap(new byte[]{1})));

        //when
        subscriber.onComplete();

        //then
        assertSame(closeFailure, getFailure(subscriber));
        assertTrue(output.closed);
    }

    @Test
    void should_preserve_close_failure_as_suppressed() {
        //given
        IOException transferFailure = new IOException("Transfer failed");
        IOException closeFailure = new IOException("Close failed");

        TrackingOutputStream output = new TrackingOutputStream();
        output.closeFailure = closeFailure;

        ResourceDownloadBodySubscriber subscriber = createSubscriber(1, output);

        subscriber.onSubscribe(new RecordingSubscription());

        //when
        subscriber.onError(transferFailure);

        //then
        Throwable failure = getFailure(subscriber);

        assertSame(transferFailure, failure);
        assertEquals(1, failure.getSuppressed().length);
        assertSame(closeFailure, failure.getSuppressed()[0]);
    }

    private static ResourceDownloadBodySubscriber createSubscriber(
            long expectedSize,
            TrackingOutputStream output
    ) {
        return new ResourceDownloadBodySubscriber(
                URL,
                TARGET_PATH,
                expectedSize,
                output
        );
    }

    private static Throwable getFailure(
            ResourceDownloadBodySubscriber subscriber
    ) {
        CompletableFuture<Void> body = subscriber.getBody()
                .toCompletableFuture();

        assertTrue(body.isDone());

        CompletionException failure = assertThrows(
                CompletionException.class,
                body::join
        );

        return failure.getCause();
    }

    private static final class RecordingSubscription implements Flow.Subscription {

        private boolean cancelled;
        private long requested;

        @Override
        public void request(long count) {
            requested += count;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private static final class TrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;
        private IOException closeFailure;

        @Override
        public void close() throws IOException {
            closed = true;

            if (closeFailure != null) {
                throw closeFailure;
            }

            super.close();
        }
    }
}
