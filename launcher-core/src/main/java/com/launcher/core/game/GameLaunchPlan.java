package com.launcher.core.game;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record GameLaunchPlan(
        Path gameDirectory,
        List<String> command
) {

    public GameLaunchPlan {
        Objects.requireNonNull(gameDirectory, "gameDirectory");
        Objects.requireNonNull(command, "command");

        if (command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }

        command = List.copyOf(command);
    }

}
