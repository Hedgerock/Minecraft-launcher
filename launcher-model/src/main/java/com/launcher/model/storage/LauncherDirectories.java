package com.launcher.model.storage;

import java.nio.file.Path;

public record LauncherDirectories(
        Path launcher,
        Path game,
        Path mods,
        Path libraries,
        Path natives,
        Path versions,
        Path assets,
        Path runtime,
        Path logs,
        Path downloads
) {
}
