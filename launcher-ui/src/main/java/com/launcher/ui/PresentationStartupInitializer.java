package com.launcher.ui;

import com.launcher.app.configuration.LauncherDirectoryConfigurationException;
import com.launcher.app.configuration.ManifestUriConfigurationException;
import com.launcher.app.configuration.path.LauncherUserPathsResolutionException;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.startup.PresentationStartupState;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class PresentationStartupInitializer {

    PresentationStartupState initialize(
            Supplier<LauncherConfiguration> configurationResolver,
            Consumer<LauncherConfiguration> boundaryInitializer
    ) {
        Objects.requireNonNull(configurationResolver, "configurationResolver");
        Objects.requireNonNull(boundaryInitializer, "boundaryInitializer");

        LauncherConfiguration configuration;

        try {
            configuration = Objects.requireNonNull(configurationResolver.get(), "configuration");
        } catch (ManifestUriConfigurationException | LauncherUserPathsResolutionException |
                 LauncherDirectoryConfigurationException exception) {
            return PresentationStartupState.CONFIGURATION_FAILED;
        }

        boundaryInitializer.accept(configuration);
        return PresentationStartupState.AVAILABLE;
    }
}
