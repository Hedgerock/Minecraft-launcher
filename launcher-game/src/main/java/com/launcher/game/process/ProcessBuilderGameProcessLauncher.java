package com.launcher.game.process;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;

import java.io.IOException;
import java.util.Objects;

public class ProcessBuilderGameProcessLauncher implements GameProcessLauncher {

    @Override
    public Process launch(GameLaunchPlan gameLaunchPlan) {
        Objects.requireNonNull(gameLaunchPlan, "gameLaunchPlan");

        try {
            return new ProcessBuilder(gameLaunchPlan.command())
                    .directory(gameLaunchPlan.gameDirectory().toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();

        } catch (IOException e) {
            throw new GameLaunchException(
                    "Failed to launch game process",
                    e
            );
        }

    }
}
