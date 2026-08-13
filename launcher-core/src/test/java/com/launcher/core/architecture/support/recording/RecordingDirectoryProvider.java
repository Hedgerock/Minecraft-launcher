package com.launcher.core.architecture.support.recording;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public final class RecordingDirectoryProvider implements DirectoryProvider {
    private LauncherDirectories directories;

    public RecordingDirectoryProvider() {
        this.directories = new LauncherDirectories(
                Path.of("launcher"),
                Path.of("launcher/game"),
                Path.of("launcher/mods"),
                Path.of("launcher/libraries"),
                Path.of("launcher/versions"),
                Path.of("assets"),
                Path.of("runtime"),
                Path.of("logs"),
                Path.of("downloads")
        );
    }

    @Override
    public LauncherDirectories directories() {
        return directories;
    }

    public void setDirectories(LauncherDirectories directories) {
        this.directories = directories;
    }
}
