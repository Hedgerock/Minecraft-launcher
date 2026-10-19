package com.launcher.game.support;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.process.ProcessBuilderGameProcessLauncher;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class GameProcessOutputProbe {

    private GameProcessOutputProbe() {
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Expected game directory and process command"
            );
        }

        GameLaunchPlan plan = new GameLaunchPlan(
                Path.of(args[0]),
                Arrays.asList(args).subList(1, args.length)
        );

        Process process = new ProcessBuilderGameProcessLauncher().launch(plan);

        try {
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Game process did not finish within the timeout");
            }

            if (process.exitValue() != 0) {
                throw new IllegalStateException(
                        "Game process exited with code " + process.exitValue()
                );
            }
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }

            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Game process remained alive after forced termination"
                );
            }
        }
    }
}
