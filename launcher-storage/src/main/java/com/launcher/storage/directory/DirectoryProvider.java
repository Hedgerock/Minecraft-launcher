package com.launcher.storage.directory;

import com.launcher.model.storage.LauncherDirectories;

import java.nio.file.Path;

public interface DirectoryProvider {
    LauncherDirectories directories();
}
