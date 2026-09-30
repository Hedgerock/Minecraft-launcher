package com.launcher.ui;

import com.launcher.app.configuration.LauncherDirectoryConfigurationException;
import com.launcher.app.configuration.LocalManifestUriConfigurationException;
import com.launcher.app.configuration.ManifestUriConfigurationException;
import com.launcher.app.configuration.path.LauncherUserPathsResolutionException;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.startup.PresentationStartupResult;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class PresentationStartupInitializer {

    PresentationStartupResult initialize(
            Supplier<LauncherConfiguration> configurationResolver,
            Consumer<LauncherConfiguration> boundaryInitializer
    ) {
        Objects.requireNonNull(configurationResolver, "configurationResolver");
        Objects.requireNonNull(boundaryInitializer, "boundaryInitializer");

        LauncherConfiguration configuration;

        try {
            configuration = Objects.requireNonNull(configurationResolver.get(), "configuration");
        } catch (LocalManifestUriConfigurationException exception) {
            return PresentationStartupResult.localConfigurationFailed();
        } catch (ManifestUriConfigurationException |
                 LauncherUserPathsResolutionException |
                 LauncherDirectoryConfigurationException exception) {
            return PresentationStartupResult.configurationFailed();
        }

        boundaryInitializer.accept(configuration);
        return PresentationStartupResult.available();
    }
}
