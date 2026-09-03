package com.launcher.core.architecture.support.recording;

import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;

public final class RecordingJavaRuntimeSelector implements JavaRuntimeSelector {
    private LaunchInfo launchInfo;

    @Override
    public JavaExecutableReference selectJavaExecutable(LaunchInfo launchInfo) {
        this.launchInfo = launchInfo;
        return JavaExecutableReference.commandName("new-java-executable");
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }
}
