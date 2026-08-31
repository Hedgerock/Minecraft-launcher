package com.launcher.natives.support;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public final class FixedDirectoryProvider implements DirectoryProvider {
    private final LauncherDirectories directories;

    public FixedDirectoryProvider(Path targetDirectory) {
        Path launcherDirectory = targetDirectory.resolve("launcher");

        this.directories = new LauncherDirectories(
                launcherDirectory,
                launcherDirectory.resolve("game"),
                launcherDirectory.resolve("mods"),
                launcherDirectory.resolve("libraries"),
                launcherDirectory.resolve("natives"),
                launcherDirectory.resolve("versions"),
                targetDirectory.resolve("assets"),
                targetDirectory.resolve("runtime"),
                targetDirectory.resolve("logs"),
                targetDirectory.resolve("downloads")
        );
    }

    @Override
    public LauncherDirectories directories() {
        return directories;
    }
}
