package com.launcher.app.configuration;

import com.launcher.app.configuration.path.LauncherUserPaths;
import com.launcher.app.configuration.path.LauncherUserPathsResolver;
import com.launcher.app.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.core.configuration.LauncherConfiguration;

import java.net.URI;
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
        PropertiesManifestUriSource manifestUriSource = new PropertiesManifestUriSource();

        LauncherUserPaths paths = args.length < 2
                ? launcherUserPathsSupplier.get()
                : null;

        URI manifestUri = args.length > 0
                ? URI.create(args[0])
                : manifestUriSource.load(paths.configurationFile());

        Path launcherDirectory = args.length > 1
                ? Path.of(args[1])
                : paths.defaultLauncherDirectory();

        return new LauncherConfiguration(manifestUri, launcherDirectory);
    }

    private static LauncherUserPaths resolveSystemUserPaths() {
        LauncherUserPathsResolver launcherUserPathsResolver = new LauncherUserPathsResolver();
        SystemRuntimeEnvironmentProvider provider = new SystemRuntimeEnvironmentProvider();

        return launcherUserPathsResolver.resolve(
                provider.current().operatingSystem()
        );
    }
}
