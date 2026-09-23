package com.launcher.app.bootstrap;

import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.support.NoOpPresentationLaunchCompletionHandler;
import com.launcher.app.support.RecordingLauncherLifecycleRunner;
import com.launcher.app.support.RecordingPresentationLaunchCompletionHandler;
import com.launcher.core.LaunchResult;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapTest {

    @Test
    void should_execute_configured_lifecycle_runner_through_presentation_boundary() throws InterruptedException {
        //given
        LaunchResult launchResult = LaunchResult.success(LauncherState.RUNNING);
        RecordingLauncherLifecycleRunner runner = new RecordingLauncherLifecycleRunner(launchResult);
        Bootstrap bootstrap = new Bootstrap(getDefaultConfiguration(), runner);

        RecordingPresentationLaunchCompletionHandler handler = new RecordingPresentationLaunchCompletionHandler();

        try (PresentationLaunchBoundary boundary = bootstrap.createPresentationLaunchBoundary(handler)) {
            //when
            LaunchRequestResult result = boundary.requestLaunch();

            assertTrue(handler.awaitHandled(5, TimeUnit.SECONDS));

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, result);
            assertEquals(runner.getLaunchResult(), handler.getResult().launchResult().orElseThrow());
        }
    }

    @Test
    void should_reject_null_launcher_result_handler_in_presentation_launch_boundary_creation() {
        //given
        Bootstrap bootstrap = new Bootstrap(getDefaultConfiguration());

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> bootstrap.createPresentationLaunchBoundary(null)
        );

        assertEquals("presentationLaunchCompletionHandler", exception.getMessage());
    }

    @Test
    void should_create_launch_presentation_launch_boundary() {
        //given
        Bootstrap bootstrap = new Bootstrap(getDefaultConfiguration());

        //when
        try (PresentationLaunchBoundary boundary =
                     bootstrap.createPresentationLaunchBoundary(new NoOpPresentationLaunchCompletionHandler())
        ) {
            //then
            assertInstanceOf(DefaultPresentationLaunchBoundary.class, boundary);
        }
    }

    @Test
    void should_reject_null_launcher_lifecycle_runner() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Bootstrap(
                        getDefaultConfiguration(),
                        null
                )
        );

        assertEquals("launcherLifecycleRunner", exception.getMessage());
    }

    @Test
    void should_reject_null_launcher_configuration() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Bootstrap(null)
        );

        assertEquals("launcherConfiguration", exception.getMessage());
    }

    @Test
    void should_create_launcher_engine_through_bootstrap() {
        //given
        LauncherConfiguration configuration = getDefaultConfiguration();

        Bootstrap bootstrap = new Bootstrap(configuration);

        //when
        LauncherEngine launcherEngine = bootstrap.createEngine();

        //then
        assertNotNull(launcherEngine);
    }

    private LauncherConfiguration getDefaultConfiguration() {
        return new LauncherConfiguration(
                URI.create("https://localhost/manifest.json"),
                Path.of("")
        );
    }
}
