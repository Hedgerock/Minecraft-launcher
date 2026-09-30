package com.launcher.ui.smoke;

import com.launcher.ui.LauncherApplication;
import com.launcher.ui.support.FxTestProbe;
import com.launcher.ui.support.fixture.LocalUiSmokeFixture;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "KEYSTONE_UI_SMOKE", matches = "true")
public class LauncherUiSmokeTest {

    @Test
    void should_show_launch_phase_and_completion_when_local_manifest_is_available(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        try (LocalUiSmokeFixture fixture = LocalUiSmokeFixture.start(tempDir)) {
            Thread launcherThread = Thread.ofPlatform().start(() -> {
                try {
                    Application.launch(
                            ObservedLauncherApplication.class,
                            fixture.getManifestUri().toString(),
                            fixture.getLauncherDirectory().toString()
                    );
                } catch (Throwable failure) {
                    ObservedLauncherApplication.STARTED.completeExceptionally(failure);
                }
            });

            Stage stage = null;

            try {
                stage = ObservedLauncherApplication.STARTED.get(10, TimeUnit.SECONDS);
                Stage activeStage = stage;

                Button launchButton = FxTestProbe.call(() ->
                        (Button) activeStage.getScene().lookup("#launch-button")
                );

                assertFalse(FxTestProbe.call(launchButton::isDisabled));

                //when
                FxTestProbe.run(launchButton::fire);

                //then
                assertTrue(fixture.awaitManifestRequest(Duration.ofSeconds(5)));

                FxTestProbe.awaitLabel(
                        activeStage,
                        "Loading manifest...",
                        Duration.ofSeconds(5)
                );

                fixture.releaseManifest();

                FxTestProbe.awaitLabel(
                        activeStage,
                        "Game launched",
                        Duration.ofSeconds(10)
                );

                assertTrue(fixture.awaitGameStartedMarker(Duration.ofSeconds(5)));
            } finally {
                fixture.releaseManifest();

                try {
                    if (stage != null) {
                        FxTestProbe.run(stage::close);
                    }
                } finally {
                    launcherThread.join(5_000);
                    assertFalse(launcherThread.isAlive());
                }
            }
        }
    }

    public static final class ObservedLauncherApplication extends LauncherApplication {
        static final CompletableFuture<Stage> STARTED = new CompletableFuture<>();

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            STARTED.complete(primaryStage);
        }
    }
}
