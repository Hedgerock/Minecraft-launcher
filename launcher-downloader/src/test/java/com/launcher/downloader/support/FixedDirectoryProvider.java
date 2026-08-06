package com.launcher.downloader.support;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public final class FixedDirectoryProvider implements DirectoryProvider {
    private final LauncherDirectories directories;

    public FixedDirectoryProvider(Path gameDirectory) {
        this.directories = new LauncherDirectories(
                Path.of("launcher"),
                gameDirectory,
                gameDirectory.resolve("mods"),
                gameDirectory.resolve("libraries"),
                gameDirectory.resolve("versions"),
                gameDirectory.resolve("assets"),
                Path.of("runtime"),
                Path.of("logs"),
                Path.of("downloads")
        );
    }

    @Override
    public LauncherDirectories directories() {
        return directories;
    }
}
