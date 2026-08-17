package com.launcher.core.game;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.model.manifest.Manifest;

public class BuildGameLaunchPlanTask implements LauncherTask {
    private final GameLaunchPlanBuilder gameLaunchPlanBuilder;

    public BuildGameLaunchPlanTask(GameLaunchPlanBuilder gameLaunchPlanBuilder) {
        this.gameLaunchPlanBuilder = gameLaunchPlanBuilder;
    }

    @Override
    public LauncherState state() {
        return LauncherState.BUILDING_GAME_LAUNCH_PLAN;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        Manifest manifest = launchContext.getManifest();

        if (manifest == null) {
            return TaskResult.failure("Manifest not loaded");
        }

        if (manifest.launchInfo() == null) {
            return TaskResult.failure("Launch info not available");
        }

        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build(manifest);
        launchContext.setGameLaunchPlan(gameLaunchPlan);
        return TaskResult.success();
    }
}
