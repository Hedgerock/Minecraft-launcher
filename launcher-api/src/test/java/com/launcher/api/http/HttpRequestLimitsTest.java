package com.launcher.api.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpRequestLimitsTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void should_reject_non_positive_max_response_bytes(int maxResponseBytes) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HttpRequestLimits(
                        Duration.of(1, TimeUnit.SECONDS.toChronoUnit()),
                        maxResponseBytes
                )
        );

        assertEquals("maxResponseBytes must be positive", exception.getMessage());
    }

    @Test
    void should_reject_large_timeout_value() {
        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new HttpRequestLimits(
                Duration.of(Long.MAX_VALUE, TimeUnit.SECONDS.toChronoUnit()),
                10
        ));

        assertEquals("timeout is too large", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L})
    void should_reject_non_positive_timeout(long timeout) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HttpRequestLimits(
                        Duration.of(timeout, TimeUnit.SECONDS.toChronoUnit()),
                        10
                )
        );

        assertEquals(
                "timeout must be positive",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_timeout() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpRequestLimits(null, 10)
        );

        assertEquals(
                "timeout",
                exception.getMessage()
        );
    }
}
