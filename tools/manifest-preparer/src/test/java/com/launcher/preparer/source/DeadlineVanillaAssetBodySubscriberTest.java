package com.launcher.preparer.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(10)
class DeadlineVanillaAssetBodySubscriberTest {

    @Test
    void should_reject_null_target_even_when_length_is_zero() {
        //given
        try (var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0)) {
            //when & then
            assertThrows(NullPointerException.class, () -> body.read(null, 0, 0));
        }
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "-1, 0", "0, -1", "0, 2"})
    void should_reject_invalid_read_range_without_requesting_data(int offset, int length) {
        //given
        byte[] target = new byte[1];
        try (var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0)) {
            RecordingSubscription subscription = new RecordingSubscription(ignored -> {
                throw new AssertionError("Invalid read range must not request data");
            });
            body.onSubscribe(subscription);

            //when & then
            assertThrows(
                    IndexOutOfBoundsException.class,
                    () -> body.read(target, offset, length)
            );
            assertTrue(subscription.requests.isEmpty());
        }
    }

    @Test
    void should_read_bytes_into_requested_slice() throws IOException {
        //given
        byte[] target = {9, 9, 9, 9};
        try (var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0)) {
            body.onSubscribe(new RecordingSubscription(ignored -> {
                body.onNext(List.of(ByteBuffer.wrap(new byte[]{1, 2, 3})));
                body.onComplete();
            }));

            //when
            int count = body.read(target, 1, 2);

            //then
            assertEquals(2, count);
            assertArrayEquals(new byte[]{9, 1, 2, 9}, target);
        }
    }

    @Test
    void should_request_next_batch_only_after_consuming_current_batch() throws IOException {
        //given
        try (var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0)) {
            RecordingSubscription subscription = new RecordingSubscription(requestNumber -> {
                if (requestNumber == 1) {
                    body.onNext(List.of(
                            ByteBuffer.wrap(new byte[]{1}),
                            ByteBuffer.wrap(new byte[]{2})
                    ));
                } else {
                    body.onNext(List.of(ByteBuffer.wrap(new byte[]{3})));
                    body.onComplete();
                }
            });

            body.onSubscribe(subscription);

            //when
            int first = body.read();
            int second = body.read();

            //then
            assertEquals(1, first);
            assertEquals(2, second);
            assertEquals(List.of(1L), subscription.requests);

            //when
            int third = body.read();
            int end = body.read();

            //then
            assertEquals(3, third);
            assertEquals(-1, end);
            assertEquals(List.of(1L, 1L), subscription.requests);
        }
    }

    @Test
    void should_timeout_when_expected_bytes_arrive_without_body_completion() throws IOException {
        //given
        AtomicLong nanoTime = new AtomicLong(60);
        try (var body = new DeadlineVanillaAssetBodySubscriber(50, 100, nanoTime::get)) {
            RecordingSubscription subscription = new RecordingSubscription(requestNumber -> {
                if (requestNumber == 1) {
                    body.onNext(List.of(ByteBuffer.wrap(new byte[]{7})));
                } else {
                    nanoTime.set(150);
                }
            });

            body.onSubscribe(subscription);

            //when
            int received = body.read();

            //then
            assertEquals(7, received);

            //when & then
            assertThrows(HttpTimeoutException.class, body::read);
            assertEquals(1, subscription.cancellations);
        }
    }

    @Test
    void should_cancel_subscription_and_restore_interrupt_flag_when_read_is_interrupted() {
        //given
        try (var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0)) {
            RecordingSubscription subscription = new RecordingSubscription(
                    ignored -> Thread.currentThread().interrupt()
            );
            body.onSubscribe(subscription);

            try {
                //when & then
                assertThrows(InterruptedIOException.class, body::read);
                assertTrue(Thread.currentThread().isInterrupted());
                assertEquals(1, subscription.cancellations);
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void should_cancel_unfinished_subscription_when_close() {
        //given
        var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0);
        RecordingSubscription subscription = new RecordingSubscription(ignored -> {});
        body.onSubscribe(subscription);

        //when
        body.close();
        body.close();

        //then
        assertEquals(1, subscription.cancellations);
        assertTrue(subscription.requests.isEmpty());
    }

    @Test
    void should_cancel_subscription_received_after_close() {
        //given
        var body = new DeadlineVanillaAssetBodySubscriber(0, 100, () -> 0);
        RecordingSubscription subscription = new RecordingSubscription(ignored -> {});
        body.close();

        //when
        body.onSubscribe(subscription);

        //then
        assertEquals(1, subscription.cancellations);
        assertTrue(subscription.requests.isEmpty());
    }

    private static final class RecordingSubscription implements Flow.Subscription {

        private final List<Long> requests = new ArrayList<>();
        private final IntConsumer onRequest;

        private int cancellations;

        private RecordingSubscription(IntConsumer onRequest) {
            this.onRequest = onRequest;
        }

        @Override
        public void request(long count) {
            requests.add(count);
            onRequest.accept(requests.size());
        }

        @Override
        public void cancel() {
            cancellations++;
        }
    }
}
