package com.launcher.app;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.result.LauncherResultHandler;
import com.launcher.app.result.NoOpLauncherResultHandler;
import com.launcher.core.LaunchResult;
import com.launcher.core.LauncherEngine;

public class Launcher {

    public static void main(String[] args) {

        ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                new LauncherConfigurationResolver().resolve(args);

        Bootstrap bootstrap = new Bootstrap(resolvedLauncherConfiguration);
        LauncherEngine launcherEngine = bootstrap.createEngine();
        LaunchResult result = launcherEngine.launch(resolvedLauncherConfiguration.configuration());

        LauncherResultHandler handler = new NoOpLauncherResultHandler();
        handler.handle(result);
    }

}
