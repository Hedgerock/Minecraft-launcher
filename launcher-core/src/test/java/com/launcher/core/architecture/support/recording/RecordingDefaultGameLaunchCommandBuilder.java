package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;

import java.util.List;

public final class RecordingDefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {

    private LaunchInfo launchInfo;
    private LaunchVariables launchVariables;

    @Override
    public List<String> build(LaunchInfo launchInfo, LaunchVariables launchVariables) {
        this.launchInfo = launchInfo;
        this.launchVariables = launchVariables;
        return List.of("test-command");
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }

    public LaunchVariables getLaunchVariables() {
        return launchVariables;
    }
}
