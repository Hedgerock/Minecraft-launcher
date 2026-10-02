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
    private static final String LOCAL_CONFIGURATION_ARGUMENT = "--local-config";

    private final Supplier<LauncherUserPaths> launcherUserPathsSupplier;
    private final BundledManifestUriSource source;
    private final ManifestUriParser parser = new DefaultManifestUriParser();

    LauncherConfigurationResolver(
            Supplier<LauncherUserPaths> launcherUserPathsSupplier,
            BundledManifestUriSource source
    ) {
        this.launcherUserPathsSupplier = Objects.requireNonNull(
                launcherUserPathsSupplier,
                "launcherUserPathsSupplier"
        );

        this.source = Objects.requireNonNull(
                source,
                "source"
        );
    }

    LauncherConfigurationResolver(Supplier<LauncherUserPaths> pathsSupplier) {
        this(
                pathsSupplier,
                new BundledManifestUriSource(
                        new DefaultManifestUriParser()
                )
        );
    }

    public LauncherConfigurationResolver() {
        this(LauncherConfigurationResolver::resolveSystemUserPaths);
    }

    public ResolvedLauncherConfiguration resolve(String[] args) {
        PropertiesManifestUriSource manifestUriSource = new PropertiesManifestUriSource(parser);

        boolean localConfigurationSelected =
                args.length > 0 && LOCAL_CONFIGURATION_ARGUMENT.equals(args[0]);

        URI explicitManifestUri = args.length > 0 && !localConfigurationSelected
                ? parser.parse(args[0])
                : null;

        LauncherUserPaths paths = localConfigurationSelected || args.length < 2
                ? launcherUserPathsSupplier.get()
                : null;

        URI manifestUri;
        ManifestSourceKind sourceKind;

        if (explicitManifestUri != null) {
            manifestUri = explicitManifestUri;
            sourceKind = ManifestSourceKind.EXPLICIT_URI;
        } else if (localConfigurationSelected) {
            manifestUri = loadLocalManifestUri(manifestUriSource, paths.configurationFile());
            sourceKind = ManifestSourceKind.LOCAL_CONFIG;
        } else {
            manifestUri = source.load();
            sourceKind = ManifestSourceKind.MANAGED;
        }

        Path launcherDirectory = args.length > 1
                ? resolveLauncherDirectory(args[1])
                : paths.defaultLauncherDirectory();

        return new ResolvedLauncherConfiguration(
                new LauncherConfiguration(manifestUri, launcherDirectory),
                sourceKind
        );
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

    private URI loadLocalManifestUri(
            PropertiesManifestUriSource source,
            Path configurationFile
    ) {
        try {
            return source.load(configurationFile);
        } catch (ManifestUriConfigurationException exception) {
            throw new LocalManifestUriConfigurationException(
                    "Local manifest URI configuration is unavailable",
                    exception
            );
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
