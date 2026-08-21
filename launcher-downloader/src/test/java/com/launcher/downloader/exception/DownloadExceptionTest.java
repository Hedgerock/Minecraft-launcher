package com.launcher.downloader.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DownloadExceptionTest {

    @Test
    void should_create_download_failed_exception() {
        //given
        IOException cause = new IOException("network failed");

        //when
        DownloadException exception = new DownloadException(
                "https://example.com/file.jar",
                cause
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals("https://example.com/file.jar", exception.getUrl());
        assertTrue(exception.getPath().isEmpty());
        assertSame(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("https://example.com/file.jar"));

    }

    @Test
    void should_create_size_mismatch_exception_with_path() {
        //given
        DownloadException exception = DownloadException.sizeMismatch(
                "https://example.com/file.jar",
                "mods/file.jar"
        );

        //then
        assertEquals(DownloadExceptionReason.SIZE_MISMATCH, exception.getReason());
        assertEquals("https://example.com/file.jar", exception.getUrl());
        assertEquals("mods/file.jar", exception.getPath().orElseThrow());
        assertTrue(exception.getMessage().contains("mods/file.jar"));
    }

    @Test
    void should_create_size_read_failed_exception_with_cause_and_path() {
        //given
        IOException cause = new IOException("access denied");

        //when
        DownloadException downloadException = DownloadException.fileSizeReadFailed(
                "https://example.com/file.jar",
                "mods/file.jar",
                cause
        );

        //then
        assertEquals(DownloadExceptionReason.SIZE_READ_FAILED, downloadException.getReason());
        assertEquals("https://example.com/file.jar", downloadException.getUrl());
        assertEquals("mods/file.jar", downloadException.getPath().orElseThrow());
        assertSame(cause, downloadException.getCause());
        assertTrue(downloadException.getMessage().contains("mods/file.jar"));

    }

}
