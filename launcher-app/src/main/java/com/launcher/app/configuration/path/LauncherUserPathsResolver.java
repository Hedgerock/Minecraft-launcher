package com.launcher.app.configuration.path;

import com.launcher.model.runtime.OperatingSystem;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class LauncherUserPathsResolver {
    private static final String APPLICATION_DIRECTORY = "keystone";
    private static final String CONFIGURATION_FILE = "keystone.properties";

    private final Map<String, String> environment;
    private final Path userHome;

    public LauncherUserPathsResolver() {
        this(
                System.getenv(),
                Path.of(System.getProperty("user.home"))
        );
    }

    LauncherUserPathsResolver(
            Map<String, String> environment,
            Path userHome
    ) {
        this.environment = Map.copyOf(
                Objects.requireNonNull(environment, "environment")
        );
        this.userHome = Objects.requireNonNull(userHome, "userHome");

        if (!userHome.isAbsolute()) {
            throw new IllegalArgumentException("userHome must be absolute");
        }
    }

    public LauncherUserPaths resolve(OperatingSystem operatingSystem) {
        Objects.requireNonNull(operatingSystem, "operatingSystem");

        Path configurationBase;
        Path dataBase;

        switch (operatingSystem) {
            case WINDOWS -> {
                Path localAppData = windowsLocalAppData();
                configurationBase = localAppData;
                dataBase = localAppData;
            }
            case LINUX -> {
                configurationBase = xdgBase("XDG_CONFIG_HOME", ".config");
                dataBase = xdgBase("XDG_DATA_HOME", ".local/share");
            }
            case MACOS -> {
                Path applicationSupport = userHome
                        .resolve("Library")
                        .resolve("Application Support");

                configurationBase = applicationSupport;
                dataBase = applicationSupport;
            }
            default -> throw new IllegalStateException(
                    "Unsupported operating system: " + operatingSystem
            );
        }

        Path configurationFile = configurationBase
                .resolve(APPLICATION_DIRECTORY)
                .resolve(CONFIGURATION_FILE);

        Path defaultLauncherDirectory = dataBase
                .resolve(APPLICATION_DIRECTORY);

        return new LauncherUserPaths(
                configurationFile,
                defaultLauncherDirectory
        );
    }

    private Path windowsLocalAppData() {
        String value = environment.get("LOCALAPPDATA");

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("LOCALAPPDATA is unavailable");
        }

        try {
            Path path = Path.of(value);

            if (!path.isAbsolute()) {
                throw new IllegalStateException("LOCALAPPDATA must be absolute");
            }

            return path;
        } catch (InvalidPathException e) {
            throw new IllegalStateException(
                    "LOCALAPPDATA is invalid",
                    e
            );
        }
    }

    private Path xdgBase(String variable, String fallback) {
        Path defaultPath = userHome.resolve(fallback);
        String value = environment.get(variable);

        if (value == null || value.isBlank()) {
            return defaultPath;
        }

        try {
            Path configuredPath = Path.of(value);

            return configuredPath.isAbsolute()
                    ? configuredPath
                    : defaultPath;
        } catch (InvalidPathException e) {
            return defaultPath;
        }
    }
}
