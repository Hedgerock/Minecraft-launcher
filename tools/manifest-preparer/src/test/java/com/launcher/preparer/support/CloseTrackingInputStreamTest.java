package com.launcher.preparer.support;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloseTrackingInputStreamTest {

    @Test
    void should_not_mark_close_before_it_is_called() {
        //given & when
        CloseTrackingInputStream input =
                new CloseTrackingInputStream(new byte[0]);

        //then
        assertFalse(input.wasCloseCalled());
        assertFalse(input.wasCloseSuccessful());
    }

    @Test
    void should_reject_null_content() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CloseTrackingInputStream(null)
        );

        assertEquals("content", exception.getMessage());
    }

    @Test
    void should_preserve_byte_array_reading_after_close() throws IOException {
        //given
        byte[] content = {10, 20, 30};

        CloseTrackingInputStream input = new CloseTrackingInputStream(content);

        input.close();

        //when
        byte[] result = input.readAllBytes();

        //then
        assertArrayEquals(content, result);
    }

    @Test
    void should_record_attempt_without_success_when_close_fails() {
        //given
        IOException failure = new IOException("Failed to close");

        CloseTrackingInputStream input = new CloseTrackingInputStream(
                new byte[0],
                failure
        );

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                input::close
        );

        assertSame(failure, exception);
        assertTrue(input.wasCloseCalled());
        assertFalse(input.wasCloseSuccessful());
    }

    @Test
    void should_record_successful_close() throws IOException {
        //given
        CloseTrackingInputStream input = new CloseTrackingInputStream(
                new byte[0]
        );

        //when
        input.close();

        //then
        assertTrue(input.wasCloseCalled());
        assertTrue(input.wasCloseSuccessful());
    }

    @Test
    void should_read_original_content() throws IOException {
        //given
        byte[] content = {0, 1, (byte) 128, (byte) 255};

        try (CloseTrackingInputStream input = new CloseTrackingInputStream(content)) {
            //when
            byte[] result = input.readAllBytes();

            //then
            assertArrayEquals(content, result);
            assertFalse(input.wasCloseCalled());
            assertFalse(input.wasCloseSuccessful());
        }
    }
}
