package com.launcher.core.task;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.state.LauncherState;
import com.launcher.core.result.Result;

public interface LauncherTask {

    LauncherState state();
    Result execute(LaunchContext launchContext);

}
