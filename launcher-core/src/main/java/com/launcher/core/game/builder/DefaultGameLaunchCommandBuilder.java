package com.launcher.core.game.builder;

import com.launcher.model.manifest.LaunchInfo;

import java.util.ArrayList;
import java.util.List;

public final class DefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {

    public List<String> build(LaunchInfo launchInfo) {
        List<String> command = new ArrayList<>();

        command.add("java");
        command.addAll(launchInfo.jvmArgs());
        command.add(launchInfo.mainClass());
        command.addAll(launchInfo.gameArgs());

        return List.copyOf(command);
    }

}
