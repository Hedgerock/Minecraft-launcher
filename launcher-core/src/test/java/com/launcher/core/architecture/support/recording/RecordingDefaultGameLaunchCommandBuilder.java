package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;

import java.util.List;

public final class RecordingDefaultGameLaunchCommandBuilder implements GameLaunchCommandBuilder {

    private LaunchInfo launchInfo;
    private LaunchVariables launchVariables;
    private JavaExecutableReference javaExecutableReference;

    @Override
    public List<String> build(
            LaunchInfo launchInfo,
            LaunchVariables launchVariables,
            JavaExecutableReference javaExecutableReference
    ) {
        this.launchInfo = launchInfo;
        this.launchVariables = launchVariables;
        this.javaExecutableReference = javaExecutableReference;
        return List.of("test-command");
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }

    public LaunchVariables getLaunchVariables() {
        return launchVariables;
    }

    public JavaExecutableReference getJavaExecutableReference() {
        return javaExecutableReference;
    }
}
