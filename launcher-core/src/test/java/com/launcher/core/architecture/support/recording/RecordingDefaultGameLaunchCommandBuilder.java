package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.model.manifest.LaunchInfo;

import java.util.List;

public final class RecordingDefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {

    private LaunchInfo launchInfo;

    @Override
    public List<String> build(LaunchInfo launchInfo) {
        this.launchInfo = launchInfo;
        return List.of("test-command");
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }
}
