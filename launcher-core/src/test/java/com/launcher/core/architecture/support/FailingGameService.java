package com.launcher.core.architecture.support;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameService;

public final class FailingGameService implements GameService {

    @Override
    public void launch(GameLaunchPlan plan) {
        throw new IllegalStateException("Game launch failed");
    }
}
