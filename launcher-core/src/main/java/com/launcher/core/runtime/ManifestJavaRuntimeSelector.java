package com.launcher.core.runtime;

import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public final class ManifestJavaRuntimeSelector implements JavaRuntimeSelector {

    @Override
    public JavaExecutableReference selectJavaExecutable(LaunchInfo launchInfo) {
        Objects.requireNonNull(launchInfo, "launchInfo");

        return JavaExecutableReference.commandName(launchInfo.javaExecutable());
    }
}
