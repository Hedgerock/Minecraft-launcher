package com.launcher.ui.phase;

import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;
import com.launcher.ui.UiThreadExecutor;
import javafx.application.Platform;

import java.util.Objects;
import java.util.function.Consumer;

public final class JavaFxPresentationLaunchPhaseHandler implements PresentationLaunchPhaseHandler {
    private final UiThreadExecutor uiThreadExecutor;
    private final Consumer<PresentationLaunchPhase> presentationLaunchPhaseConsumer;

    public JavaFxPresentationLaunchPhaseHandler(
            Consumer<PresentationLaunchPhase> presentationLaunchPhaseConsumer
    ) {
        this(Platform::runLater, presentationLaunchPhaseConsumer);
    }

    JavaFxPresentationLaunchPhaseHandler(
            UiThreadExecutor uiThreadExecutor,
            Consumer<PresentationLaunchPhase> presentationLaunchPhaseConsumer
    ) {
        this.uiThreadExecutor = Objects.requireNonNull(
                uiThreadExecutor,
                "uiThreadExecutor"
        );
        this.presentationLaunchPhaseConsumer = Objects.requireNonNull(
                presentationLaunchPhaseConsumer,
                "presentationLaunchPhaseConsumer"
        );
    }

    @Override
    public void handle(PresentationLaunchPhase phase) {
        Objects.requireNonNull(phase, "phase");

        uiThreadExecutor.execute(() ->
                presentationLaunchPhaseConsumer.accept(phase));
    }
}
