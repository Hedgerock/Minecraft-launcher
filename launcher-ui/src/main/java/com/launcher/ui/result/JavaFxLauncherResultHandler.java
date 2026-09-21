package com.launcher.ui.result;

import com.launcher.app.result.LauncherResultHandler;
import com.launcher.core.LaunchResult;
import javafx.application.Platform;

import java.util.Objects;
import java.util.function.Consumer;

public final class JavaFxLauncherResultHandler implements LauncherResultHandler {
    private final UiThreadExecutor uiThreadExecutor;
    private final Consumer<LaunchResult> resultConsumer;

    public JavaFxLauncherResultHandler(Consumer<LaunchResult> resultConsumer) {
        this(Platform::runLater, resultConsumer);
    }

    JavaFxLauncherResultHandler(
            UiThreadExecutor uiThreadExecutor,
            Consumer<LaunchResult> resultConsumer
    ) {
        this.uiThreadExecutor = Objects.requireNonNull(
                uiThreadExecutor,
                "uiThreadExecutor"
        );
        this.resultConsumer = Objects.requireNonNull(
                resultConsumer,
                "resultConsumer"
        );
    }

    @Override
    public void handle(LaunchResult result) {
        Objects.requireNonNull(result, "result");

        uiThreadExecutor.execute(() -> resultConsumer.accept(result));
    }
}
