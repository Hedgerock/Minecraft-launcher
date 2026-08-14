package com.launcher.game.service;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameService;
import com.launcher.game.process.GameProcessLauncher;

public class DefaultGameService implements GameService {
    private final GameProcessLauncher gameProcessLauncher;

    public DefaultGameService(GameProcessLauncher gameProcessLauncher) {
        this.gameProcessLauncher = gameProcessLauncher;
    }

    @Override
    public void launch(GameLaunchPlan plan) {

        gameProcessLauncher.launch(plan);
    }
}
