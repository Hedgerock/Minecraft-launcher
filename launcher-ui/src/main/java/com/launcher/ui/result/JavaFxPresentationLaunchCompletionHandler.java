package com.launcher.ui.result;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.ui.UiThreadExecutor;
import javafx.application.Platform;

import java.util.Objects;
import java.util.function.Consumer;

public final class JavaFxPresentationLaunchCompletionHandler implements PresentationLaunchCompletionHandler {
    private final UiThreadExecutor uiThreadExecutor;
    private final Consumer<PresentationLaunchCompletion> presentationLaunchCompletionConsumer;

    public JavaFxPresentationLaunchCompletionHandler(Consumer<PresentationLaunchCompletion> presentationLaunchCompletionConsumer) {
        this(Platform::runLater, presentationLaunchCompletionConsumer);
    }

    JavaFxPresentationLaunchCompletionHandler(
            UiThreadExecutor uiThreadExecutor,
            Consumer<PresentationLaunchCompletion> presentationLaunchCompletionConsumer
    ) {
        this.uiThreadExecutor = Objects.requireNonNull(
                uiThreadExecutor,
                "uiThreadExecutor"
        );
        this.presentationLaunchCompletionConsumer = Objects.requireNonNull(
                presentationLaunchCompletionConsumer,
                "presentationLaunchCompletionConsumer"
        );
    }

    @Override
    public void handle(PresentationLaunchCompletion completion) {
        Objects.requireNonNull(completion, "completion");

        uiThreadExecutor.execute(() -> presentationLaunchCompletionConsumer.accept(
                completion
        ));
    }
}
