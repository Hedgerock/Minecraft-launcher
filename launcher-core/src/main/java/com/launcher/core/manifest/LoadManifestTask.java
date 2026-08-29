package com.launcher.core.manifest;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.model.manifest.Manifest;
import com.launcher.core.result.Result;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;

public class LoadManifestTask implements LauncherTask {

    private final ManifestService manifestService;

    public LoadManifestTask(ManifestService manifestService) {
        this.manifestService = manifestService;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();

        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        launchContext.setManifest(manifest);
        launchContext.setRuntimeLibrarySelection(runtimeLibrarySelection);

        return TaskResult.success();
    }

    @Override
    public LauncherState state() {
        return LauncherState.LOADING_MANIFEST;
    }
}
