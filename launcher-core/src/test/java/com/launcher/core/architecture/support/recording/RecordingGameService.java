package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameService;

public final class RecordingGameService implements GameService {
    private boolean isLaunchCalled;
    private final GameLaunchPlan receivedGameLaunchPlan;

    public RecordingGameService(GameLaunchPlan receivedGameLaunchPlan) {
        this.isLaunchCalled = false;
        this.receivedGameLaunchPlan = receivedGameLaunchPlan;
    }

    @Override
    public void launch(GameLaunchPlan plan) {
        isLaunchCalled = true;
    }

    public boolean isLaunchCalled() {
        return isLaunchCalled;
    }

    public GameLaunchPlan getReceivedGameLaunchPlan() {
        return receivedGameLaunchPlan;
    }
}
