package com.launcher.core.game;

import com.launcher.core.storage.directory.DirectoryProvider;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;

    public GameLaunchPlanBuilder(DirectoryProvider directoryProvider) {
        this.directoryProvider = directoryProvider;
    }

    public GameLaunchPlan build() {
        return new GameLaunchPlan(
                directoryProvider.directories().game()
        );
    }

}
