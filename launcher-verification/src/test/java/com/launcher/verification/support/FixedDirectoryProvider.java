package com.launcher.verification.support;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public final class FixedDirectoryProvider implements DirectoryProvider {

    private final LauncherDirectories directories;

    public FixedDirectoryProvider(Path launcherDirectory) {
        this.directories = new LauncherDirectories(
                launcherDirectory,
                launcherDirectory,
                launcherDirectory.resolve("mods"),
                launcherDirectory.resolve("libraries"),
                launcherDirectory.resolve("versions"),
                launcherDirectory.resolve("assets"),
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
