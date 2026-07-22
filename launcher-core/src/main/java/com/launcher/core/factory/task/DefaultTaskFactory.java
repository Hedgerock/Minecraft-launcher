package com.launcher.core.factory.task;

import com.launcher.core.directory.PrepareDirectoriesTask;
import com.launcher.core.manifest.LoadManifestTask;
import com.launcher.core.service.LauncherServices;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class DefaultTaskFactory implements TaskFactory {
    private final LauncherServices launcherServices;

    public DefaultTaskFactory(LauncherServices launcherServices) {
        this.launcherServices = launcherServices;
    }

    @Override
    public List<LauncherTask> createTasks() {
        return List.of(
                new PrepareDirectoriesTask(launcherServices.directoryService()),
                new LoadManifestTask(launcherServices.manifestService())
        );
    }
}
