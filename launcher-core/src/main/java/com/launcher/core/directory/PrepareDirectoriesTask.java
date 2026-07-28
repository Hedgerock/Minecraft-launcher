package com.launcher.core.directory;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.storage.service.DirectoryService;

public class PrepareDirectoriesTask implements LauncherTask {

    private final DirectoryService directoryService;

    public PrepareDirectoriesTask(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @Override
    public LauncherState state() {
        return null;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        directoryService.prepareLauncherDirectories();
        return new SuccessResult();
    }
}
