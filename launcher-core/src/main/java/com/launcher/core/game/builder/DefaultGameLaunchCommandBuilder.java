package com.launcher.core.game.builder;

import com.launcher.core.resolve.LaunchArgumentResolver;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;

import java.util.ArrayList;
import java.util.List;

public final class DefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {
    private final LaunchArgumentResolver launchArgumentResolver;

    public DefaultGameLaunchCommandBuilder(
            LaunchArgumentResolver launchArgumentResolver
    ) {
        this.launchArgumentResolver = launchArgumentResolver;
    }

    public List<String> build(LaunchInfo launchInfo, LaunchVariables launchVariables) {
        List<String> command = new ArrayList<>();

        command.add("java");
        command.addAll(launchArgumentResolver.resolve(launchInfo.jvmArgs(), launchVariables));
        command.add(launchInfo.mainClass());
        command.addAll(launchArgumentResolver.resolve(launchInfo.gameArgs(), launchVariables));

        return List.copyOf(command);
    }

}
