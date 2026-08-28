package com.launcher.core.architecture.storage;

import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingFileStorage;
import com.launcher.core.storage.service.DefaultDirectoryService;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.model.storage.LauncherDirectories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultDirectoryServiceTest {

    @Test
    void should_create_directories_using_directory_provider_paths() {
        //given
        RecordingDirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        RecordingFileStorage fileStorage = new RecordingFileStorage();

        Path launcherDirectory = Path.of("custom/launcher");
        Path gameDirectory = Path.of("custom/game");
        Path modsDirectory = Path.of("custom/mods");
        Path assetsDirectory = Path.of("custom/assets");
        Path librariesDirectory = Path.of("custom/libraries");
        Path nativesDirectory = Path.of("custom/natives");
        Path downloadsDirectory = Path.of("custom/downloads");
        Path versionsDirectory = Path.of("custom/versions");
        Path runtimeDirectory = Path.of("custom/runtime");
        Path logsDirectory = Path.of("custom/logs");


        directoryProvider.setDirectories(
                new LauncherDirectories(
                        launcherDirectory,
                        gameDirectory,
                        modsDirectory,
                        librariesDirectory,
                        nativesDirectory,
                        versionsDirectory,
                        assetsDirectory,
                        runtimeDirectory,
                        logsDirectory,
                        downloadsDirectory
                )
        );

        DirectoryService service = new DefaultDirectoryService(
                directoryProvider,
                fileStorage
        );

        //when
        service.prepareLauncherDirectories();

        //then
        assertEquals(
               List.of(
                       launcherDirectory,
                       gameDirectory,
                       modsDirectory,
                       assetsDirectory,
                       librariesDirectory,
                       nativesDirectory,
                       downloadsDirectory,
                       versionsDirectory,
                       runtimeDirectory,
                       logsDirectory
               ),
                fileStorage.getCreatedDirectories()
        );

    }

    @Test
    void should_create_launcher_directories() {

        //given
        RecordingDirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        RecordingFileStorage fileStorage = new RecordingFileStorage();

        DirectoryService service = new DefaultDirectoryService(
                directoryProvider,
                fileStorage
        );

        //when
        service.prepareLauncherDirectories();

        //then
        assertEquals(
                List.of(
                        Path.of("launcher"),
                        Path.of("launcher/game"),
                        Path.of("launcher/mods"),
                        Path.of("assets"),
                        Path.of("launcher/libraries"),
                        Path.of("launcher/natives"),
                        Path.of("downloads"),
                        Path.of("launcher/versions"),
                        Path.of("runtime"),
                        Path.of("logs")
                ),
                fileStorage.getCreatedDirectories()
        );

    }

}
