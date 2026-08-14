package com.launcher.game.support;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;
import com.launcher.game.process.GameProcessLauncher;

public class RecordingGameProcessLauncher implements GameProcessLauncher {
    private GameLaunchPlan plan;
    private final boolean withException;

    public RecordingGameProcessLauncher() {
        this.withException = false;
    }

    public RecordingGameProcessLauncher(boolean withException) {
        this.withException = withException;
    }

    @Override
    public Process launch(GameLaunchPlan plan) {
        if (withException) {
            throw new GameLaunchException("Failed to launch game");
        }

        this.plan = plan;

        return null;
    }

    public GameLaunchPlan getPlan() {
        return plan;
    }
}
