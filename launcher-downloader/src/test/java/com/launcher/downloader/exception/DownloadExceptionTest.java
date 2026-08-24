package com.launcher.downloader.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DownloadExceptionTest {

    @Test
    void should_add_resource_path_and_preserve_original_exception_as_cause() {
        //given
        IOException cause = new IOException("download failed");

        DownloadException exception =
                DownloadException.downloadFailed(
                        "http://file-entry.jar",
                        Path.of("game/mods/current-mode.jar"),
                        cause
                );

        //when
        DownloadException result = exception.withPath("mods/current-mode.jar");

        //then
        assertEquals(
                "mods/current-mode.jar",
                result.getPath().orElseThrow()
        );

        assertSame(cause, result.getCause());
    }

    @Test
    void should_create_download_failed_exception() {
        //given
        IOException cause = new IOException("network failed");
        Path path = Path.of("current-path");

        //when
        DownloadException exception = DownloadException.downloadFailed(
                "https://example.com/file.jar",
                "current-path",
                path,
                cause
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals("https://example.com/file.jar", exception.getUrl());
        assertEquals("current-path", exception.getPath().orElseThrow());
        assertTrue(exception.getMessage().contains("current-path"));
        assertEquals(path, exception.getTargetPath().orElseThrow());
        assertSame(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("https://example.com/file.jar"));

    }

    @Test
    void should_create_size_mismatch_exception_with_path() {
        //given
        Path path = Path.of("target-path");
        DownloadException exception = DownloadException.sizeMismatch(
                "https://example.com/file.jar",
                "mods/file.jar",
                path
        );

        //then
        assertEquals(DownloadExceptionReason.SIZE_MISMATCH, exception.getReason());
        assertEquals("https://example.com/file.jar", exception.getUrl());
        assertEquals("mods/file.jar", exception.getPath().orElseThrow());
        assertEquals(path, exception.getTargetPath().orElseThrow());
        assertNull(exception.getCause());
        assertTrue(exception.getMessage().contains("mods/file.jar"));
    }

    @Test
    void should_create_size_read_failed_exception_with_cause_and_path() {
        //given
        Path path = Path.of("target-path");
        IOException cause = new IOException("access denied");

        //when
        DownloadException downloadException = DownloadException.sizeReadFailed(
                "https://example.com/file.jar",
                "mods/file.jar",
                path,
                cause
        );

        //then
        assertEquals(DownloadExceptionReason.SIZE_READ_FAILED, downloadException.getReason());
        assertEquals("https://example.com/file.jar", downloadException.getUrl());
        assertEquals("mods/file.jar", downloadException.getPath().orElseThrow());
        assertEquals(path, downloadException.getTargetPath().orElseThrow());
        assertSame(cause, downloadException.getCause());
        assertTrue(downloadException.getMessage().contains("mods/file.jar"));

    }

}
