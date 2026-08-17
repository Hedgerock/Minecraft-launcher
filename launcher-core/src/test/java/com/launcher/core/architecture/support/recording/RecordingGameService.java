package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameService;

public final class RecordingGameService implements GameService {
    private boolean isLaunchCalled;
    private GameLaunchPlan receivedGameLaunchPlan;

    @Override
    public void launch(GameLaunchPlan plan) {
        isLaunchCalled = true;
        this.receivedGameLaunchPlan = plan;
    }

    public boolean isLaunchCalled() {
        return isLaunchCalled;
    }

    public GameLaunchPlan getReceivedGameLaunchPlan() {
        return receivedGameLaunchPlan;
    }
}
