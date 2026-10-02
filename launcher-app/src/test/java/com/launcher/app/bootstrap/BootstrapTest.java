package com.launcher.app.bootstrap;

import com.launcher.app.configuration.ManifestSourceKind;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.LauncherLifecycleRunner;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.support.NoOpPresentationLaunchCompletionHandler;
import com.launcher.app.support.RecordingLauncherLifecycleRunner;
import com.launcher.app.support.RecordingPresentationLaunchCompletionHandler;
import com.launcher.app.support.RecordingPresentationLaunchPhaseHandler;
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
    void should_execute_presentation_phase_handler_through_presentation_boundary() throws InterruptedException {
        //given
        LaunchResult launchResult =
                LaunchResult.success(LauncherState.RUNNING);

        PresentationLaunchPhase expectedPhase =
                PresentationLaunchPhase.LOADING_MANIFEST;

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(expectedPhase);

            return launchResult;
        };

        Bootstrap bootstrap =
                new Bootstrap(getResolvedLauncherConfiguration(), runner);

        RecordingPresentationLaunchCompletionHandler completionHandler =
                new RecordingPresentationLaunchCompletionHandler();

        RecordingPresentationLaunchPhaseHandler phaseHandler =
                new RecordingPresentationLaunchPhaseHandler();

        try (PresentationLaunchBoundary boundary = bootstrap.createPresentationLaunchBoundary(
                completionHandler,
                phaseHandler
        )) {
            //when
            LaunchRequestResult requestResult = boundary.requestLaunch();

            assertTrue(
                    completionHandler.awaitHandled(5, TimeUnit.SECONDS)
            );

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    requestResult
            );

            assertEquals(
                    expectedPhase,
                    phaseHandler.getPhase()
            );
        }
    }

    @Test
    void should_execute_configured_lifecycle_runner_through_presentation_boundary() throws InterruptedException {
        //given
        LaunchResult launchResult = LaunchResult.success(LauncherState.RUNNING);
        RecordingLauncherLifecycleRunner runner = new RecordingLauncherLifecycleRunner(launchResult);
        Bootstrap bootstrap = new Bootstrap(getResolvedLauncherConfiguration(), runner);

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
        Bootstrap bootstrap = new Bootstrap(getResolvedLauncherConfiguration());

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
        Bootstrap bootstrap = new Bootstrap(getResolvedLauncherConfiguration());

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
                        getResolvedLauncherConfiguration(),
                        null
                )
        );

        assertEquals("launcherLifecycleRunner", exception.getMessage());
    }

    @Test
    void should_reject_null_resolved_launcher_configuration() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Bootstrap(null)
        );

        assertEquals("resolvedLauncherConfiguration", exception.getMessage());
    }

    @Test
    void should_reject_null_resolved_launcher_configuration_for_package_private_constructor() {
        //given
        LaunchResult launchResult =
                LaunchResult.success(LauncherState.RUNNING);

        PresentationLaunchPhase expectedPhase =
                PresentationLaunchPhase.LOADING_MANIFEST;

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(expectedPhase);

            return launchResult;
        };

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Bootstrap(null, runner)
        );

        assertEquals("resolvedLauncherConfiguration", exception.getMessage());
    }

    @Test
    void should_create_launcher_engine_through_bootstrap() {
        //given
        Bootstrap bootstrap = new Bootstrap(getResolvedLauncherConfiguration());

        //when
        LauncherEngine launcherEngine = bootstrap.createEngine();

        //then
        assertNotNull(launcherEngine);
    }

    private ResolvedLauncherConfiguration getResolvedLauncherConfiguration() {
        return new ResolvedLauncherConfiguration(
                new LauncherConfiguration(
                        URI.create("https://localhost/manifest.json"),
                        Path.of("")
                ),
                ManifestSourceKind.EXPLICIT_URI
        );
    }
}
