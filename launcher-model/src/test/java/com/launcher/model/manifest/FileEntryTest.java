package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileEntryTest {

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        new FileEntry(
                                "non-blank-path",
                                "sha-256",
                                -123L,
                                "https://example.com"
                        )
        );

        assertTrue(
                exception.getMessage().contains("size must be positive")
        );
    }

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        new FileEntry(
                                "non-blank-path",
                                "sha-256",
                                123L,
                                " "
                        )
        );

        assertTrue(
                exception.getMessage().contains("url must not be blank")
        );
    }

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->
                        new FileEntry(
                                "non-blank-path",
                                "sha-256",
                                123L,
                                null
                        )
        );

        assertTrue(
                exception.getMessage().contains("url")
        );
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        new FileEntry(
                                "non-blank-path",
                                " ",
                                123L,
                                "https://example.com"
                        )
        );

        assertTrue(
                exception.getMessage().contains("sha256 must not be blank")
        );
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->
                        new FileEntry(
                                "non-blank-path",
                                null,
                                123L,
                                "https://example.com"
                        )
        );

        assertTrue(
                exception.getMessage().contains("sha256")
        );
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        new FileEntry(
                                " ",
                                "sha256",
                                123L,
                                "https://example.com"
                        )
        );

        assertTrue(
                exception.getMessage().contains("path must not be blank")
        );
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->
                        new FileEntry(
                                null,
                                "sha256",
                                123L,
                                "https://example.com"
                        )
        );

        assertTrue(
                exception.getMessage().contains("path")
        );
    }

}
