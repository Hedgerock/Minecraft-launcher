package com.launcher.ui;

import com.launcher.app.configuration.LauncherDirectoryConfigurationException;
import com.launcher.app.configuration.LocalManifestUriConfigurationException;
import com.launcher.app.configuration.ManifestUriConfigurationException;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.configuration.path.LauncherUserPathsResolutionException;
import com.launcher.ui.startup.PresentationStartupResult;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class PresentationStartupInitializer {

    PresentationStartupResult initialize(
            Supplier<ResolvedLauncherConfiguration> configurationResolver,
            Consumer<ResolvedLauncherConfiguration> boundaryInitializer
    ) {
        Objects.requireNonNull(configurationResolver, "configurationResolver");
        Objects.requireNonNull(boundaryInitializer, "boundaryInitializer");

        ResolvedLauncherConfiguration resolvedLauncherConfiguration;

        try {
            resolvedLauncherConfiguration = Objects.requireNonNull(
                    configurationResolver.get(),
                    "resolvedLauncherConfiguration"
            );
        } catch (LocalManifestUriConfigurationException exception) {
            return PresentationStartupResult.localConfigurationFailed();
        } catch (ManifestUriConfigurationException |
                 LauncherUserPathsResolutionException |
                 LauncherDirectoryConfigurationException exception) {
            return PresentationStartupResult.configurationFailed();
        }

        boundaryInitializer.accept(resolvedLauncherConfiguration);
        return PresentationStartupResult.available();
    }
}
