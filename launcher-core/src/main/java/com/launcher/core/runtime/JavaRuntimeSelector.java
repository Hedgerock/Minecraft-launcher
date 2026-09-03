package com.launcher.core.runtime;

import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaRuntimeSelector {

    JavaExecutableReference selectJavaExecutable(LaunchInfo launchInfo);

}
