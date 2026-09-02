package com.launcher.core.runtime;

import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;
import java.util.Objects;

public final class ManifestJavaRuntimeSelector implements JavaRuntimeSelector {

    @Override
    public Path selectJavaExecutable(LaunchInfo launchInfo) {
        Objects.requireNonNull(launchInfo, "launchInfo");

        return Path.of(launchInfo.javaExecutable());
    }
}
