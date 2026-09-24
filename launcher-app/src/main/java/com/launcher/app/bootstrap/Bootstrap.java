package com.launcher.app.bootstrap;

import com.launcher.app.assembly.ApplicationAssembly;
import com.launcher.app.assembly.DefaultApplicationAssembly;
import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
import com.launcher.app.presentation.LauncherLifecycleRunner;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.app.presentation.report.DefaultPresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;

import java.util.Objects;

public final class Bootstrap {
    private final LauncherConfiguration launcherConfiguration;
    private final LauncherLifecycleRunner launcherLifecycleRunner;

    public Bootstrap(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = Objects.requireNonNull(
                launcherConfiguration,
                "launcherConfiguration"
        );
        this.launcherLifecycleRunner =
                () -> createEngine().launch(this.launcherConfiguration);
    }

    Bootstrap(
            LauncherConfiguration launcherConfiguration,
            LauncherLifecycleRunner launcherLifecycleRunner
    ) {
        this.launcherConfiguration = Objects.requireNonNull(
                launcherConfiguration,
                "launcherConfiguration"
        );
        this.launcherLifecycleRunner = Objects.requireNonNull(
                launcherLifecycleRunner,
                "launcherLifecycleRunner"
        );
    }

    public PresentationLaunchBoundary createPresentationLaunchBoundary(
            PresentationLaunchCompletionHandler presentationLaunchCompletionHandler
    ) {
        Objects.requireNonNull(presentationLaunchCompletionHandler, "presentationLaunchCompletionHandler");

        PresentationLaunchDiagnosticReporter reporter =
                new DefaultPresentationLaunchDiagnosticReporter();

        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                presentationLaunchCompletionHandler,
                reporter
        );
    }

    public LauncherEngine createEngine() {
        ApplicationAssembly applicationAssembly = new DefaultApplicationAssembly(launcherConfiguration);
        return applicationAssembly.createEngine();
    }

}
