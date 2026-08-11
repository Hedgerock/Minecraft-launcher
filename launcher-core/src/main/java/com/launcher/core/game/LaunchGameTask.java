package com.launcher.core.game;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;

public class LaunchGameTask implements LauncherTask {
    private final GameService gameService;

    public LaunchGameTask(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public LauncherState state() {
        return LauncherState.LAUNCHING;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        gameService.launch();

        return TaskResult.success();
    }
}
