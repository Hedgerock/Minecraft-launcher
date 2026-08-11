package com.launcher.core.operation.impl;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.game.GameService;
import com.launcher.core.game.LaunchGameTask;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class LaunchGameOperation extends LaunchOperation {

    private final GameService gameService;

    public LaunchGameOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            GameService gameService
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.LAUNCH_GAME,
                eventBus
        );
        this.gameService = gameService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new LaunchGameTask(gameService)
        );
    }
}
