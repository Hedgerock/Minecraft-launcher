package com.launcher.app.runtime.detection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeVersionProbeLimitsTest {

    private static final Duration EXECUTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration CLEANUP_TIMEOUT = Duration.ofSeconds(5);
    private static final int OUTPUT_LIMIT = 64 * 1024;

    @Test
    void should_use_default_probe_limits() {
        //given & when
        JavaRuntimeVersionProbeLimits result = JavaRuntimeVersionProbeLimits.defaults();

        //then
        assertEquals(EXECUTION_TIMEOUT, result.executionTimeout());
        assertEquals(OUTPUT_LIMIT, result.maxOutputBytes());
        assertEquals(CLEANUP_TIMEOUT, result.cleanupTimeout());
    }

    @Test
    void should_preserve_probe_limits() {
        //given
        Duration executionTimeout = Duration.ofNanos(1);
        Duration cleanupTimeout = Duration.ofNanos(2);

        //when
        JavaRuntimeVersionProbeLimits result =
                new JavaRuntimeVersionProbeLimits(
                        executionTimeout,
                        1,
                        cleanupTimeout
                );

        //then
        assertEquals(executionTimeout, result.executionTimeout());
        assertEquals(1, result.maxOutputBytes());
        assertEquals(cleanupTimeout, result.cleanupTimeout());
    }

    @Test
    void should_reject_null_execution_timeout() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeVersionProbeLimits(
                        null,
                        OUTPUT_LIMIT,
                        CLEANUP_TIMEOUT
                )
        );

        assertEquals("executionTimeout", exception.getMessage());
    }

    @Test
    void should_reject_null_cleanup_timeout() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaRuntimeVersionProbeLimits(
                        EXECUTION_TIMEOUT,
                        OUTPUT_LIMIT,
                        null
                )
        );

        assertEquals("cleanupTimeout", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidTimeouts")
    void should_reject_invalid_execution_timeout(Duration timeout) {
        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new JavaRuntimeVersionProbeLimits(
                        timeout,
                        OUTPUT_LIMIT,
                        CLEANUP_TIMEOUT
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTimeouts")
    void should_reject_invalid_cleanup_timeout(Duration timeout) {
        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new JavaRuntimeVersionProbeLimits(
                        EXECUTION_TIMEOUT,
                        OUTPUT_LIMIT,
                        timeout
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {
            Integer.MIN_VALUE,
            -1,
            0,
            Integer.MAX_VALUE
    })
    void should_reject_invalid_output_limit(int maxOutputBytes) {
        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new JavaRuntimeVersionProbeLimits(
                        EXECUTION_TIMEOUT,
                        maxOutputBytes,
                        CLEANUP_TIMEOUT
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64 * 1024, Integer.MAX_VALUE - 1})
    void should_accept_valid_output_limit(int maxOutputBytes) {
        //given & when
        JavaRuntimeVersionProbeLimits result =
                new JavaRuntimeVersionProbeLimits(
                        EXECUTION_TIMEOUT,
                        maxOutputBytes,
                        CLEANUP_TIMEOUT
                );

        //then
        assertEquals(
                maxOutputBytes,
                result.maxOutputBytes()
        );
    }

    private static Stream<Duration> invalidTimeouts() {
        return Stream.of(
                Duration.ZERO,
                Duration.ofNanos(-1),
                Duration.ofSeconds(Long.MAX_VALUE)
        );
    }
}
