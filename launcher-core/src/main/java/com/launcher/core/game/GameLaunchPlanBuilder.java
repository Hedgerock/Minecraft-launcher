package com.launcher.core.game;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.Manifest;

import java.util.ArrayList;
import java.util.List;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;

    public GameLaunchPlanBuilder(DirectoryProvider directoryProvider) {
        this.directoryProvider = directoryProvider;
    }

    public GameLaunchPlan build(Manifest manifest) {
        return new GameLaunchPlan(
                directoryProvider.directories().game(),
                buildCommand(manifest)
        );

    }

    private List<String> buildCommand(Manifest manifest) {
        LaunchInfo launchInfo = manifest.launchInfo();

        List<String> command = new ArrayList<>();

        command.add("java");
        command.addAll(launchInfo.jvmArgs());
        command.add(launchInfo.mainClass());
        command.addAll(launchInfo.gameArgs());

        return List.copyOf(command);
    }


}
