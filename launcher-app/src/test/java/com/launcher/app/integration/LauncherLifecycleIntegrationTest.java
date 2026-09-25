package com.launcher.app.integration;

import com.launcher.app.assembly.DefaultApplicationAssembly;
import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.support.JsonProvider;
import com.launcher.app.support.LocalServerStarter;
import com.launcher.core.LaunchResult;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.state.LauncherState;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LauncherLifecycleIntegrationTest {
    private final LocalServerStarter localServerStarter = new LocalServerStarter();

    @Test
    void should_deliver_ordered_phases_before_completion_when_launcher_lifecycle_succeeds(@TempDir Path tempDir)
            throws IOException, InterruptedException {
        //given
        String path = "/manifest.json";
        HttpServer server = localServerStarter.getServer(JsonProvider.MANIFEST_JSON, path);
        server.start();

        try {
            URI manifestUri = URI.create("http://localhost:" + server.getAddress().getPort() + path);
            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = createFakeJavaExecutable(tempDir, gameStartedMarker);

            LauncherConfiguration config = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            List<Object> callbacks = new CopyOnWriteArrayList<>();
            AtomicReference<PresentationLaunchCompletion> receivedCompletion =
                    new AtomicReference<>();

            CountDownLatch completionHandled = new CountDownLatch(1);

            Bootstrap bootstrap = new Bootstrap(config);

            try (PresentationLaunchBoundary boundary =
                    bootstrap.createPresentationLaunchBoundary(
                            completion -> {
                                receivedCompletion.set(completion);
                                callbacks.add(completion);
                                completionHandled.countDown();
                            },
                            callbacks::add
                    )
            ) {
                //when
                LaunchRequestResult requestResult = boundary.requestLaunch();

                assertTrue(completionHandled.await(10, TimeUnit.SECONDS));

                //then
                assertEquals(
                        LaunchRequestResult.ACCEPTED,
                        requestResult
                );

                PresentationLaunchCompletion completion = receivedCompletion.get();

                assertNotNull(completion);
                assertFalse(completion.executionFailed());
                assertTrue(completion.launchResult().orElseThrow().success());

                List<PresentationLaunchPhase> phases = callbacks.stream()
                        .filter(PresentationLaunchPhase.class::isInstance)
                        .map(PresentationLaunchPhase.class::cast)
                        .toList();

                assertEquals(
                        List.of(
                                PresentationLaunchPhase.LOADING_MANIFEST,
                                PresentationLaunchPhase.VERIFYING_FILES,
                                PresentationLaunchPhase.PREPARING_GAME,
                                PresentationLaunchPhase.STARTING_GAME
                        ),
                        withoutAdjacentDuplicates(phases)
                );

                assertEquals(phases.size() + 1, callbacks.size());
                assertSame(completion, callbacks.getLast());

                waitUntilExists(gameStartedMarker);
            }
        } finally {
            server.stop(0);
        }
    }

    @Test
    void should_return_success_result_when_launcher_lifecycle_completed_through_application_assembly(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        String path = "/manifest.json";
        HttpServer server = localServerStarter.getServer(JsonProvider.MANIFEST_JSON, path);
        server.start();

        try {
            URI manifestUri = URI.create("http://localhost:" + server.getAddress().getPort() + path);
            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = createFakeJavaExecutable(tempDir, gameStartedMarker);

            LauncherConfiguration config = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            DefaultApplicationAssembly assembly = new DefaultApplicationAssembly(config);

            LauncherEngine engine = assembly.createEngine();

            //when
            LaunchResult result = engine.launch(config);

            //then
            assertTrue(result.success());
            assertEquals(LauncherState.RUNNING, result.finalState());
            waitUntilExists(gameStartedMarker);
        } finally {
            server.stop(0);
        }

    }

    private List<PresentationLaunchPhase> withoutAdjacentDuplicates(
            List<PresentationLaunchPhase> phases
    ) {
        List<PresentationLaunchPhase> result = new ArrayList<>();

        for (PresentationLaunchPhase phase : phases) {
            if (result.isEmpty() || result.getLast() != phase) {
                result.add(phase);
            }
        }

        return result;
    }

    private void waitUntilExists(Path path) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (Files.exists(path)) {
                return;
            }

            Thread.sleep(10);
        }

        assertTrue(
                Files.exists(path),
                "Expected fake game process marker to be created"
        );
    }

    private Path createFakeJavaExecutable(Path tempDir, Path gameStartedMarker) throws IOException {
        boolean windows = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win");

        Path executable = tempDir.resolve(
                windows ? "fake-java.cmd" : "fake-java.sh"
        );

        String markerPath = gameStartedMarker.toAbsolutePath().toString();

        String windowsCommand = """
                @echo off
                if "%%~1"=="-version" (
                 echo openjdk version "21.0.8" 2025-07-15 1>&2
                 exit /b 0
                )
                cd /d "%%TEMP%%"
                echo started > "%s"
                exit /b 0
                """.formatted(markerPath);

        String otherOsCommand = """
                #!/usr/bin/env sh
                if [ "$1" = "-version" ]; then
                 printf '%%s\\n' 'openjdk version "21.0.8" 2025-07-15' >&2
                 exit 0
                fi
                cd /tmp
                printf '%%s\\n' 'started' > '%s'
                exit 0
                """.formatted(markerPath);

        String content = windows
                ? windowsCommand
                : otherOsCommand;

        Files.writeString(executable, content, StandardCharsets.UTF_8);

        if (!windows) {
            executable.toFile().setExecutable(true);
        }

        return executable;
    }
}
