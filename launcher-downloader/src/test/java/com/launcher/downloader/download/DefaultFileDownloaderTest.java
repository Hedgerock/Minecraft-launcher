package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFileDownloaderTest {
    private static final String TEST_FILE_CONTENT = "Hello test!";
    private static final String FAKE_URL = "not-a-url";

    @Test
    void should_delete_temporary_file_when_stream_fails_during_download(@TempDir Path tempDir) throws Exception {
        //given
        FileDownloader downloader = new DefaultFileDownloader(url -> new InputStream() {
            private int reads;

            @Override
            public int read() throws IOException {
                if (reads++ < 3) {
                    return 'a';
                }

                throw new IOException("Failed to read");
            }
        });

        Path target = tempDir.resolve("mods/test.jar");

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download("test-url", target)
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertFalse(Files.exists(target));

        try (Stream<Path> files = Files.list(target.getParent())) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    @Test
    void should_not_leave_partial_file_when_download_fails(@TempDir Path tempDir) throws IOException {
        //given
        FileDownloader downloader = new DefaultFileDownloader();
        Path target = tempDir.resolve("mods/test.jar");

        //when
        assertThrows(
                RuntimeException.class,
                () -> downloader.download(FAKE_URL, target)
        );

        //then
        Path parent = target.getParent();

        assertTrue(Files.exists(parent));

        try(Stream<Path> files = Files.list(parent)) {
            assertFalse(files.findAny().isPresent());
        }
    }

    @Test
    void should_fail_when_url_is_not_valid(@TempDir Path tempDir) {
        //given
        FileDownloader downloader = new DefaultFileDownloader();
        Path target = tempDir.resolve("mods/test.jar");

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download(FAKE_URL, target)
        );

        //then
        assertTrue(
                exception.getMessage().contains("Failed to download file: " + FAKE_URL)
        );

        assertFalse(Files.exists(target));

        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(FAKE_URL, exception.getUrl());
        assertTrue(exception.getPath().isEmpty());
    }

    @Test
    void should_replace_existing_file(@TempDir Path tempDir) throws IOException {
        //given
        Path target = tempDir.resolve("mods/test.jar");

        Files.createDirectories(target.getParent());
        Files.writeString(target, "old");

        FileDownloader downloader = new DefaultFileDownloader();


        //when
        downloader.download(sourceUrl(), target);

        //then
        assertEquals(
                TEST_FILE_CONTENT,
                Files.readString(target)
        );
    }

    @Test
    void should_download_file_to_target_path(@TempDir Path tempDir) throws IOException {
        //given
        FileDownloader downloader = new DefaultFileDownloader();

        Path target = tempDir
                .resolve("mods/test.jar");

        //when
        downloader.download(
                sourceUrl(),
                target
        );

        //then
        assertEquals(
                TEST_FILE_CONTENT,
                Files.readString(target)
        );
    }

    @Test
    void should_create_parent_directories(@TempDir Path tempDir) {
        //given
        FileDownloader downloader = new DefaultFileDownloader();
        Path target = tempDir.resolve("mods/subfolder/test.jar");

        //when
        downloader.download(
                sourceUrl(),
                target
        );

        //then
        assertTrue(Files.exists(target.getParent()));
        assertTrue(Files.exists(target));
    }

    @SuppressWarnings("ConstantConditions")
    private String sourceUrl() {

        return getClass()
                .getResource("/test-file.txt")
                .toExternalForm();
    }
}
