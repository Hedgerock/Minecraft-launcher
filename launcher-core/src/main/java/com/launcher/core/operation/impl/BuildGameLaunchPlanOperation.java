package com.launcher.core.operation.impl;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.game.BuildGameLaunchPlanTask;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class BuildGameLaunchPlanOperation extends LaunchOperation {
    private final GameLaunchPlanBuilder gameLaunchPlanBuilder;

    public BuildGameLaunchPlanOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            GameLaunchPlanBuilder gameLaunchPlanBuilder
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.BUILD_GAME_LAUNCH_PLAN,
                eventBus
        );
        this.gameLaunchPlanBuilder = gameLaunchPlanBuilder;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new BuildGameLaunchPlanTask(gameLaunchPlanBuilder)
        );
    }
}
