package com.launcher.app.configuration;

import com.launcher.app.configuration.path.LauncherUserPaths;
import com.launcher.app.configuration.path.LauncherUserPathsResolver;
import com.launcher.app.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.core.configuration.LauncherConfiguration;

import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public final class LauncherConfigurationResolver {
    private final Supplier<LauncherUserPaths> launcherUserPathsSupplier;

    LauncherConfigurationResolver(Supplier<LauncherUserPaths> launcherUserPathsSupplier) {
        this.launcherUserPathsSupplier = Objects.requireNonNull(
                launcherUserPathsSupplier,
                "launcherUserPathsSupplier"
        );
    }

    public LauncherConfigurationResolver() {
        this(LauncherConfigurationResolver::resolveSystemUserPaths);
    }

    public LauncherConfiguration resolve(String[] args) {
        ManifestUriParser parser = new DefaultManifestUriParser();
        PropertiesManifestUriSource manifestUriSource = new PropertiesManifestUriSource(parser);

        URI explicitManifestUri = args.length > 0
                ? parser.parse(args[0])
                : null;

        LauncherUserPaths paths = args.length < 2
                ? launcherUserPathsSupplier.get()
                : null;

        URI manifestUri = explicitManifestUri != null
                ? explicitManifestUri
                : manifestUriSource.load(paths.configurationFile());

        Path launcherDirectory = args.length > 1
                ? resolveLauncherDirectory(args[1])
                : paths.defaultLauncherDirectory();

        return new LauncherConfiguration(manifestUri, launcherDirectory);
    }

    private Path resolveLauncherDirectory(String value) {
        Objects.requireNonNull(value, "value");

        if (value.isBlank()) {
            throw new LauncherDirectoryConfigurationException(
                    "Launcher directory path cannot be blank"
            );
        }

        try {
            return Path.of(value);
        } catch (InvalidPathException e) {
            throw new LauncherDirectoryConfigurationException(
                    "Invalid launcher directory path", e);
        }
    }

    private static LauncherUserPaths resolveSystemUserPaths() {
        LauncherUserPathsResolver launcherUserPathsResolver = new LauncherUserPathsResolver();
        SystemRuntimeEnvironmentProvider provider = new SystemRuntimeEnvironmentProvider();

        return launcherUserPathsResolver.resolve(
                provider.current().operatingSystem()
        );
    }
}
