package com.launcher.ui.support;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class FxTestProbe {
    private FxTestProbe() {}

    public static <T> T call(Supplier<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.get();
        }

        CompletableFuture<T> result = new CompletableFuture<>();

        Platform.runLater(() -> {
           try {
               result.complete(action.get());
           } catch (Throwable failure) {
               result.completeExceptionally(failure);
           }
        });

        return result.get(5, TimeUnit.SECONDS);
    }

    public static void run(Runnable action) throws Exception {
        call(() -> {
            action.run();
            return null;
        });
    }

    public static void awaitLabel(Stage stage, String expected, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            boolean found = call(() ->
                    stage.getScene().getRoot().lookupAll(".label").stream()
                            .filter(Label.class::isInstance)
                            .map(Label.class::cast)
                            .anyMatch(label -> expected.equals(label.getText()))
            );

            if (found) {
                return;
            }

            Thread.sleep(25);
        }

        throw new AssertionError("Label not shown: " + expected);
    }
}
