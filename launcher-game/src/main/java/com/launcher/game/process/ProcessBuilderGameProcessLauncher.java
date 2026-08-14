package com.launcher.game.process;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;

import java.io.IOException;

public class ProcessBuilderGameProcessLauncher implements GameProcessLauncher {

    @Override
    public Process launch(GameLaunchPlan plan) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(plan.command());

            return processBuilder
                    .directory(plan.gameDirectory().toFile())
                    .start();

        } catch (IOException e) {
            throw new GameLaunchException(
                    "Failed to launch game process",
                    e
            );
        }

    }
}
