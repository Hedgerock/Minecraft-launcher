package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;
import java.util.List;

public final class RecordingDefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {

    private LaunchInfo launchInfo;
    private LaunchVariables launchVariables;
    private Path javaExecutable;

    @Override
    public List<String> build(
            LaunchInfo launchInfo,
            LaunchVariables launchVariables,
            Path javaExecutable
    ) {
        this.launchInfo = launchInfo;
        this.launchVariables = launchVariables;
        this.javaExecutable = javaExecutable;
        return List.of("test-command");
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }

    public LaunchVariables getLaunchVariables() {
        return launchVariables;
    }

    public Path getJavaExecutable() {
        return javaExecutable;
    }
}
