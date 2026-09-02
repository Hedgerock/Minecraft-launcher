package com.launcher.core.game.builder;

import com.launcher.core.resolve.LaunchArgumentResolver;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {
    private final LaunchArgumentResolver launchArgumentResolver;

    public DefaultGameLaunchCommandBuilder(
            LaunchArgumentResolver launchArgumentResolver
    ) {
        this.launchArgumentResolver = launchArgumentResolver;
    }

    public List<String> build(
            LaunchInfo launchInfo,
            LaunchVariables launchVariables,
            Path javaExecutable
    ) {
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(launchVariables, "launchVariables");
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        List<String> command = new ArrayList<>();

        command.add(javaExecutable.toString());
        command.addAll(launchArgumentResolver.resolve(launchInfo.jvmArgs(), launchVariables));
        command.add(launchInfo.mainClass());
        command.addAll(launchArgumentResolver.resolve(launchInfo.gameArgs(), launchVariables));

        return List.copyOf(command);
    }

}
