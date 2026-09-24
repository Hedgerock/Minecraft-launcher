package com.launcher.app.presentation;

import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;
import com.launcher.core.LaunchResult;

@FunctionalInterface
public interface LauncherLifecycleRunner {

    LaunchResult launch(PresentationLaunchPhaseHandler phaseHandler);
}
