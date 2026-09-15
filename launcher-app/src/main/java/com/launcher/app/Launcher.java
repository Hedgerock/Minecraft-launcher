package com.launcher.app;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.result.LauncherResultHandler;
import com.launcher.app.result.NoOpLauncherResultHandler;
import com.launcher.core.LaunchResult;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;

public class Launcher {

    public static void main(String[] args) {

        LauncherConfiguration configuration =
                new LauncherConfigurationResolver().resolve(args);

        Bootstrap bootstrap = new Bootstrap(configuration);
        LauncherEngine launcherEngine = bootstrap.createEngine();
        LaunchResult result = launcherEngine.launch(configuration);

        LauncherResultHandler handler = new NoOpLauncherResultHandler();
        handler.handle(result);
    }

}
