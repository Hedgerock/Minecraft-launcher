package com.launcher.downloader.service;

import com.launcher.core.download.DownloadPlan;
import com.launcher.core.download.DownloadService;
import com.launcher.downloader.support.FixedDirectoryProvider;
import com.launcher.downloader.support.RecordingFileDownloader;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDownloadServiceTest {

    private FileEntry fileEntry(String path, long size, String url) {
        return new FileEntry(
                path,
                "sha-" + path,
                size,
                url
        );
    }

    @Test
    void should_download_each_file_to_game_directory() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        FileEntry firstFile = fileEntry("first.jar", 100L, "http://first.jar");
        FileEntry secondFile = fileEntry("second.jar", 200L, "http://second.jar");

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
    void should_create_target_path_from_game_directory_and_file_path() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader
        );

        FileEntry fileEntry = fileEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));

        //when
        service.download(plan);

        //then
        Path actualPath = Path.of("/game/mods/current-mode.jar");

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

        FileEntry fileEntry = fileEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

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
