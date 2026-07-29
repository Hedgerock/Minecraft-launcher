package com.launcher.core.manifest;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.model.manifest.Manifest;
import com.launcher.core.result.Result;

public class LoadManifestTask implements LauncherTask {

    private final ManifestService manifestService;

    public LoadManifestTask(ManifestService manifestService) {
        this.manifestService = manifestService;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        Manifest manifest = manifestService.loadManifest();
        launchContext.setManifest(manifest) ;
        return TaskResult.success();
    }

    @Override
    public LauncherState state() {
        return LauncherState.LOADING_MANIFEST;
    }
}
