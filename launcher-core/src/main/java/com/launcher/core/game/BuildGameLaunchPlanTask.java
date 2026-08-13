package com.launcher.core.game;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;

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
        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build();

        launchContext.setGameLaunchPlan(gameLaunchPlan);
        return TaskResult.success();
    }
}
