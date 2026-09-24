package com.launcher.app.bootstrap;

import com.launcher.app.assembly.ApplicationAssembly;
import com.launcher.app.assembly.DefaultApplicationAssembly;
import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
import com.launcher.app.presentation.LauncherLifecycleRunner;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.app.presentation.phase.NoOpPresentationLaunchPhaseHandler;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseMapper;
import com.launcher.app.presentation.report.DefaultPresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventListener;
import com.launcher.core.event.events.StateChangedEvent;

import java.util.Objects;

public final class Bootstrap {
    private final LauncherConfiguration launcherConfiguration;
    private final LauncherLifecycleRunner launcherLifecycleRunner;
    private final PresentationLaunchPhaseMapper presentationLaunchPhaseMapper = new PresentationLaunchPhaseMapper();

    public Bootstrap(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = Objects.requireNonNull(
                launcherConfiguration,
                "launcherConfiguration"
        );
        this.launcherLifecycleRunner =
                phaseHandler -> {
                    EventListener<StateChangedEvent> listener = event ->
                            presentationLaunchPhaseMapper.map(event.newState())
                                    .ifPresent(phaseHandler::handle);

                    return createEngine(listener).launch(launcherConfiguration);
        };
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
            PresentationLaunchCompletionHandler presentationLaunchCompletionHandler,
            PresentationLaunchPhaseHandler phaseHandler
    ) {
        Objects.requireNonNull(presentationLaunchCompletionHandler, "presentationLaunchCompletionHandler");

        PresentationLaunchDiagnosticReporter reporter =
                new DefaultPresentationLaunchDiagnosticReporter();

        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                presentationLaunchCompletionHandler,
                reporter,
                phaseHandler
        );
    }

    public PresentationLaunchBoundary createPresentationLaunchBoundary(
            PresentationLaunchCompletionHandler presentationLaunchCompletionHandler
    ) {
        return createPresentationLaunchBoundary(
                presentationLaunchCompletionHandler,
                new NoOpPresentationLaunchPhaseHandler()
        );
    }

    private LauncherEngine createEngine(EventListener<StateChangedEvent> eventListener) {
        ApplicationAssembly applicationAssembly = new DefaultApplicationAssembly(launcherConfiguration);
        return applicationAssembly.createEngine(eventListener);
    }

    public LauncherEngine createEngine() {
        return createEngine(event -> {});
    }
}
