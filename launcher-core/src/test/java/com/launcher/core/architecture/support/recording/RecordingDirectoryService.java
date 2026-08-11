package com.launcher.core.architecture.support.recording;

import com.launcher.core.storage.service.DirectoryService;

public final class RecordingDirectoryService implements DirectoryService {
    private boolean prepareLauncherDirectoriesCalled = false;
    @Override
    public void prepareLauncherDirectories() {
        prepareLauncherDirectoriesCalled = true;
    }

    public boolean isPrepareLauncherDirectoriesCalled() {
        return prepareLauncherDirectoriesCalled;
    }
}
