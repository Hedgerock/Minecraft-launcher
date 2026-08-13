package com.launcher.core.game;

import java.nio.file.Path;

public record GameLaunchPlan(
        Path gameDirectory
) {
}
