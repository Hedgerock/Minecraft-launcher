package com.launcher.verification.support;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public final class FixedDirectoryProvider implements DirectoryProvider {

    private final LauncherDirectories directories;

    public FixedDirectoryProvider(Path launcherDirectory, Path gameDirectory) {
        this.directories = new LauncherDirectories(
                launcherDirectory,
                gameDirectory,
                gameDirectory.resolve("mods"),
                gameDirectory.resolve("libraries"),
                gameDirectory.resolve("natives"),
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
