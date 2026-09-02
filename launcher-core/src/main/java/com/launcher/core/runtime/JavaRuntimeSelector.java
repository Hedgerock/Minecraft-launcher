package com.launcher.core.runtime;

import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;

public interface JavaRuntimeSelector {

    Path selectJavaExecutable(LaunchInfo launchInfo);

}
