package com.launcher.storage.service;

import com.launcher.model.storage.LauncherDirectories;
import com.launcher.storage.directory.DirectoryProvider;
import com.launcher.storage.file.FileStorage;

import java.nio.file.Path;

public class DefaultDirectoryService implements DirectoryService {
    private final DirectoryProvider directoryProvider;
    private final FileStorage fileStorage;

    public DefaultDirectoryService(DirectoryProvider directoryProvider, FileStorage fileStorage) {
        this.directoryProvider = directoryProvider;
        this.fileStorage = fileStorage;
    }

    private void create(Path path) {
        fileStorage.createDirectories(path);
    }

    @Override
    public void prepareLauncherDirectories() {
        LauncherDirectories launcherDirectories = directoryProvider.directories();

        create(launcherDirectories.launcher());
        create(launcherDirectories.game());
        create(launcherDirectories.mods());
        create(launcherDirectories.assets());
        create(launcherDirectories.libraries());
        create(launcherDirectories.versions());
        create(launcherDirectories.runtime());
        create(launcherDirectories.logs());
    }
}
