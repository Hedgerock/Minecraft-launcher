package com.launcher.app;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;

public class Launcher {

    public static void main(String[] args) {

        LauncherConfiguration configuration =
                new LauncherConfigurationResolver().resolve(args);

        Bootstrap bootstrap = new Bootstrap(configuration);
        LauncherEngine launcherEngine = bootstrap.createEngine();
        launcherEngine.launch(configuration);
    }

}
