package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import com.launcher.downloader.support.FixedDirectoryProvider;
import com.launcher.storage.resolver.DirectoryRedirectFixture;
import com.launcher.storage.resolver.LocalWriteTargetResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultFileDownloaderTest {
    private static final String TEST_FILE_CONTENT = "Hello test!";
    private static final String FAKE_URL = "not-a-url";

    @Test
    void should_download_file_when_parent_redirects_inside_trusted_root(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedRoot = tempDir.resolve("game");
        Path actualDirectory = trustedRoot.resolve("actual");
        Path targetPath = trustedRoot.resolve("redirected/library.jar");

        Files.createDirectories(actualDirectory);

        DirectoryRedirectFixture.create(
                trustedRoot.resolve("redirected"),
                actualDirectory
        );

        FileDownloader downloader = new DefaultFileDownloader(
                new LocalWriteTargetResolver()
        );

        //when
        downloader.download(sourceUrl(), trustedRoot, targetPath);

        //then
        assertEquals(
                TEST_FILE_CONTENT,
                Files.readString(actualDirectory.resolve("library.jar"))
        );
    }

    @Test
    void should_not_open_download_source_when_parent_redirects_outside_trusted_root(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedRoot = tempDir.resolve("game");
        Path outsideDirectory = tempDir.resolve("outside");
        Path targetPath = trustedRoot.resolve("redirected/library.jar");

        Files.createDirectories(trustedRoot);
        Files.createDirectories(outsideDirectory);

        DirectoryRedirectFixture.create(
                trustedRoot.resolve("redirected"),
                outsideDirectory
        );

        AtomicBoolean sourceOpened = new AtomicBoolean();

        FileDownloader downloader = new DefaultFileDownloader(
                url -> {
                    sourceOpened.set(true);
                    return InputStream.nullInputStream();
                },
                new LocalWriteTargetResolver()
        );

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download("test-uri", trustedRoot, targetPath)
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(targetPath, exception.getTargetPath().orElseThrow());
        assertInstanceOf(IOException.class, exception.getCause());
        assertFalse(sourceOpened.get());

        try (Stream<Path> files = Files.list(outsideDirectory)) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    @Test
    void should_not_open_download_source_when_target_is_outside_trusted_root(
            @TempDir Path tempDir
    ) {
        //given
        Path trustedRoot = tempDir.resolve("game");
        Path targetPath = tempDir.resolve("outside/library.jar");
        AtomicBoolean sourceOpened = new AtomicBoolean();

        FileDownloader downloader = new DefaultFileDownloader(
                url -> {
                    sourceOpened.set(true);
                    return InputStream.nullInputStream();
                },
                new LocalWriteTargetResolver()
        );

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download("test-uri", trustedRoot, targetPath)
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(targetPath, exception.getTargetPath().orElseThrow());
        assertFalse(sourceOpened.get());
        assertFalse(Files.exists(targetPath.getParent()));
    }

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
        }, new LocalWriteTargetResolver());

        Path target = tempDir.resolve("mods/test.jar");

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download(
                        "test-url",
                        new FixedDirectoryProvider(tempDir).directories().game(),
                        target
                )
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertFalse(Files.exists(target));

        try (Stream<Path> files = Files.list(target.getParent())) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    @Test
    void should_include_target_path_when_download_failed(
            @TempDir Path tempDir
    ) {
        //given
        FileDownloader downloader = new DefaultFileDownloader(new LocalWriteTargetResolver());
        Path targetPath = tempDir.resolve("mods/test.jar");

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download(FAKE_URL, getTrustedRoot(tempDir), targetPath)
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(FAKE_URL, exception.getUrl());
        assertEquals(targetPath, exception.getTargetPath().orElseThrow());
    }

    @Test
    void should_not_leave_partial_file_when_download_fails(@TempDir Path tempDir) throws IOException {
        //given
        FileDownloader downloader = new DefaultFileDownloader(new LocalWriteTargetResolver());
        Path target = tempDir.resolve("mods/test.jar");

        //when
        assertThrows(
                DownloadException.class,
                () -> downloader.download(FAKE_URL, getTrustedRoot(tempDir), target)
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
        FileDownloader downloader = new DefaultFileDownloader(new LocalWriteTargetResolver());
        Path target = tempDir.resolve("mods/test.jar");

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> downloader.download(FAKE_URL, getTrustedRoot(tempDir), target)
        );

        //then
        assertTrue(
                exception.getMessage().contains("Failed to download resource")
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

        FileDownloader downloader = new DefaultFileDownloader(new LocalWriteTargetResolver());

        //when
        downloader.download(sourceUrl(), getTrustedRoot(tempDir), target);

        //then
        assertEquals(
                TEST_FILE_CONTENT,
                Files.readString(target)
        );
    }

    @Test
    void should_download_file_to_target_path(@TempDir Path tempDir) throws IOException {
        //given
        FileDownloader downloader = new DefaultFileDownloader(new LocalWriteTargetResolver());

        Path target = tempDir
                .resolve("mods/test.jar");

        //when
        downloader.download(
                sourceUrl(),
                getTrustedRoot(tempDir),
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
        FileDownloader downloader = new DefaultFileDownloader(
                new LocalWriteTargetResolver()
        );
        Path target = tempDir.resolve("mods/subfolder/test.jar");

        //when
        downloader.download(
                sourceUrl(),
                getTrustedRoot(tempDir),
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

    private Path getTrustedRoot(Path tempDir) {
        return new FixedDirectoryProvider(tempDir).directories().game();
    }
}
