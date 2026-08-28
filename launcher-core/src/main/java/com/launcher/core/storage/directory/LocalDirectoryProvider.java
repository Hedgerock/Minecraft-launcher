package com.launcher.core.storage.directory;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public class LocalDirectoryProvider implements DirectoryProvider {

    private final LauncherDirectories launcherDirectories;

    public LocalDirectoryProvider(LauncherConfiguration configuration) {
        Path launcher = configuration.launcherDirectory();
        Path game = launcher.resolve("game");
        this.launcherDirectories = new LauncherDirectories(
                launcher,
                game,
                game.resolve("mods"),
                game.resolve("libraries"),
                game.resolve("natives"),
                game.resolve("versions"),
                game.resolve("assets"),
                launcher.resolve("runtime"),
                launcher.resolve("logs"),
                launcher.resolve("downloads")
        );
    }

    @Override
    public LauncherDirectories directories() {
        return this.launcherDirectories;
    }
}
