package com.launcher.app.presentation;

import com.launcher.core.LaunchResult;

@FunctionalInterface
public interface LauncherLifecycleRunner {

    LaunchResult launch();
}
