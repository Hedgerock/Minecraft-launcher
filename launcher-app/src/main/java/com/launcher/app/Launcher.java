package com.launcher.app;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;

import java.net.URI;
import java.nio.file.Paths;

public class Launcher {

    public static void main(String[] args) {
        //TODO Убрать хардкод
        LauncherConfiguration configuration = new LauncherConfiguration(
                URI.create("https://localhost:8080/manifest.json"),
                Paths.get("")
        );

        Bootstrap bootstrap = new Bootstrap(configuration);
        LauncherEngine launcherEngine = bootstrap.createEngine();
        launcherEngine.launch(configuration);
    }

}
