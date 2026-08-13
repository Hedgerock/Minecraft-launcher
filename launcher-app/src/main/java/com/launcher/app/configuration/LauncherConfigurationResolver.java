package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;

import java.net.URI;
import java.nio.file.Path;

public final class LauncherConfigurationResolver {

    public LauncherConfiguration resolve(String[] args) {

        URI manifestUri = args.length > 0
                ? URI.create(args[0])
                : URI.create("https://localhost:8080/manifest.json");

        Path launcherDirectory = args.length > 1
                ? Path.of(args[1])
                : Path.of("");

        return new LauncherConfiguration(manifestUri, launcherDirectory);
    }

}
