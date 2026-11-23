package com.launcher.downloader.download;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceDownloadLimitsTest {

    @Test
    void should_preserve_configured_limits() {
        //given
        Duration timeout = Duration.ofSeconds(17);
        long maxResourceBytes = 3L * 1024 * 1024 * 1024;

        //when
        ResourceDownloadLimits limits = new ResourceDownloadLimits(
                timeout,
                maxResourceBytes
        );

        //then
        assertEquals(timeout, limits.timeout());
        assertEquals(maxResourceBytes, limits.maxResourceBytes());
    }

    @Test
    void should_provide_default_limits() {
        //given & when
        ResourceDownloadLimits limits = ResourceDownloadLimits.defaults();

        //then
        assertEquals(Duration.ofMinutes(10), limits.timeout());
        assertEquals(4L * 1024 * 1024 * 1024, limits.maxResourceBytes());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 4_294_967_296L})
    void should_accept_expected_size_within_limit(long expectedSize) {
        //given
        ResourceDownloadLimits limits = ResourceDownloadLimits.defaults();

        //when & then
        assertDoesNotThrow(() -> limits.validateExpectedSize(expectedSize));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, Long.MIN_VALUE})
    void should_reject_negative_expected_size(long expectedSize) {
        //given
        ResourceDownloadLimits limits = ResourceDownloadLimits.defaults();

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> limits.validateExpectedSize(expectedSize)
        );

        assertEquals(
                "expectedSize must not be negative",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {4_294_967_297L, Long.MAX_VALUE})
    void should_reject_expected_size_exceeding_limit(long expectedSize) {
        //given
        ResourceDownloadLimits limits = ResourceDownloadLimits.defaults();

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> limits.validateExpectedSize(expectedSize)
        );

        assertEquals(
                "expectedSize exceeds maxResourceBytes",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L})
    void should_reject_non_positive_max_resource_bytes(long maxResourceBytes) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceDownloadLimits(
                        Duration.ofSeconds(10),
                        maxResourceBytes
                )
        );

        assertEquals(
                "maxResourceBytes must be positive",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void should_reject_non_positive_timeout(long seconds) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceDownloadLimits(Duration.ofSeconds(seconds), 1024)
        );

        assertEquals("timeout must be positive", exception.getMessage());
    }

    @Test
    void should_reject_null_timeout() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceDownloadLimits(null, 1024)
        );

        assertEquals("timeout", exception.getMessage());
    }

    @Test
    void should_reject_timeout_that_overflows_nanoseconds() {
        //given
        Duration timeout = Duration.ofSeconds(Long.MAX_VALUE);

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceDownloadLimits(timeout, 1024)
        );

        assertEquals("timeout is too large", exception.getMessage());
        assertInstanceOf(ArithmeticException.class, exception.getCause());
    }
}
