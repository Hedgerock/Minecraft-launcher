package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;

public final class RecordingJavaRuntimeSelector implements JavaRuntimeSelector {
    private LaunchInfo launchInfo;
    private final Path javaExecutable = Path.of("new-java-executable");

    @Override
    public Path selectJavaExecutable(LaunchInfo launchInfo) {
        this.launchInfo = launchInfo;
        return javaExecutable;
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }
}
