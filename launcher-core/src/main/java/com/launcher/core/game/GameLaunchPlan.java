package com.launcher.core.game;

import java.nio.file.Path;
import java.util.List;

public record GameLaunchPlan(
        Path gameDirectory,
        List<String> command
) {

    public GameLaunchPlan {
        command = List.copyOf(command);
    }

}
