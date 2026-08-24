package com.launcher.downloader.service;

import com.launcher.core.download.DownloadService;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import com.launcher.downloader.support.FixedDirectoryProvider;
import com.launcher.downloader.support.RecordingFileDownloader;
import com.launcher.downloader.support.WritingFileDownloader;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDownloadServiceTest {

    private ResourceEntry getResourceEntry(String path, long size, String url) {
        return new ResourceEntry(
                path,
                "sha-" + path,
                size,
                url
        );
    }

    @Test
    void should_continue_when_downloaded_file_size_matches_manifest_entry(@TempDir Path tempDir) throws IOException {
        //given
        String content = "Hello test";

        ResourceEntry fileEntry = getResourceEntry(
                "mods/file.jar",
                content.getBytes(StandardCharsets.UTF_8).length,
                "http://file.jar"
        );

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));
        Path gameDirectory = tempDir.resolve("game");

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                new WritingFileDownloader(content)
        );
        //when
        service.download(plan);

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertTrue(Files.exists(target));
        assertEquals(content, Files.readString(target));
    }

    @Test
    void should_fail_when_downloaded_file_size_can_not_written(@TempDir Path tempDir) {
        //given
        ResourceEntry fileEntry = getResourceEntry("mods/file.jar", 12L, "http://file.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        downloader.setCreateFile(false);

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        //when

        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertFalse(Files.exists(target));

        assertEquals("http://file.jar", exception.getUrl());
        assertEquals(DownloadExceptionReason.SIZE_READ_FAILED, exception.getReason());
        assertTrue(exception.getMessage().contains("Failed to get file size"));
        assertTrue(exception.getMessage().contains("mods/file.jar"));
        assertEquals("mods/file.jar", exception.getPath().orElseThrow());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void should_fail_when_downloaded_file_size_does_not_match_manifest_entry(@TempDir Path tempDir)
            throws IOException {
        //given
        ResourceEntry fileEntry = getResourceEntry("mods/file.jar", 12L, "http://file.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));
        Path gameDirectory = tempDir.resolve("game");

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                new WritingFileDownloader("Hello test")
        );

        //when

        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertTrue(Files.exists(target));
        assertEquals(10L, Files.size(target));
        assertTrue(exception.getMessage().contains("Downloaded file size mismatch"));
        assertTrue(exception.getMessage().contains("mods/file.jar"));

        assertEquals(DownloadExceptionReason.SIZE_MISMATCH, exception.getReason());
        assertEquals("http://file.jar", exception.getUrl());
        assertEquals("mods/file.jar", exception.getPath().orElseThrow());

    }

    @Test
    void should_download_each_file_to_game_directory(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        ResourceEntry firstFile = getResourceEntry("first.jar", 100L, "http://first.jar");
        ResourceEntry secondFile = getResourceEntry("second.jar", 100L, "http://second.jar");

        DownloadPlan plan = new DownloadPlan(List.of(firstFile, secondFile));

        //when
        service.download(plan);

        //then
        assertEquals(
                2,
                downloader.getRequests().size()
        );

    }

    @Test
    void should_create_target_path_from_game_directory_and_file_path(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        ResourceEntry fileEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));

        //when
        service.download(plan);

        //then
        Path actualPath = gameDirectory.resolve("mods/current-mode.jar");

        assertEquals(
                actualPath,
                downloader.getRequests()
                        .getFirst()
                        .targetPath()
        );
    }

    @Test
    void should_propagate_downloader_failure() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader(true);
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        ResourceEntry fileEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));

        //then
        assertThrows(
                RuntimeException.class,
                () -> service.download(plan)
        );

    }

    @Test
    void should_not_download_anything_when_download_plan_is_empty() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        DownloadPlan plan = new DownloadPlan(List.of());

        //when

        service.download(plan);

        //then
        assertTrue(downloader.getRequests().isEmpty());
    }

}
