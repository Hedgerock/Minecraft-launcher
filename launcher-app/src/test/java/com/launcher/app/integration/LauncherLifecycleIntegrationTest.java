package com.launcher.app.integration;

import com.launcher.app.assembly.DefaultApplicationAssembly;
import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.ManifestSourceKind;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.storage.directory.LocalDirectoryProvider;
import com.launcher.app.support.JsonProvider;
import com.launcher.app.support.LauncherHttpTestRoutes;
import com.launcher.core.LaunchResult;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.state.LauncherState;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.testing.http.LocalHttpServer;
import com.launcher.testing.process.FakeJavaExecutableFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LauncherLifecycleIntegrationTest {
    private final LauncherHttpTestRoutes httpRoutes = new LauncherHttpTestRoutes();

    @Test
    void should_launch_with_downloaded_client_asset_index_and_asset_object(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        byte[] clientBytes = "test-client-artifact".getBytes(StandardCharsets.UTF_8);
        byte[] objectBytes = "test-asset-object".getBytes(StandardCharsets.UTF_8);

        String objectSha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest(objectBytes));

        String objectRelativePath = "assets/objects/"
                + objectSha1.substring(0, 2)
                + "/"
                + objectSha1;

        byte[] indexBytes = """
                {
                    "objects": {
                        "test/object": {
                            "hash": "%s",
                            "size": %d
                        }
                    }
                }
                """.formatted(objectSha1, objectBytes.length)
                .getBytes(StandardCharsets.UTF_8);

        try (LocalHttpServer server = LocalHttpServer.bind()) {
            String clientUrl = server.uri("/client.jar").toString();
            String indexUrl = server.uri("/assets/indexes/17.json").toString();
            String objectUrl = server.uri("/" + objectRelativePath).toString();
            URI manifestUri = server.uri("/manifest.json");

            ResourceEntry client = new ResourceEntry(
                    "versions/1.21.1/client.jar",
                    sha256(clientBytes),
                    clientBytes.length,
                    clientUrl
            );

            AssetIndexMetadata index = new AssetIndexMetadata(
                    "17",
                    "assets/indexes/17.json",
                    sha256(indexBytes),
                    indexBytes.length,
                    indexUrl
            );

            ResourceEntry assetObject = new ResourceEntry(
                    objectRelativePath,
                    sha256(objectBytes),
                    objectBytes.length,
                    objectUrl
            );

            AtomicInteger clientRequests = new AtomicInteger();
            AtomicInteger indexRequests = new AtomicInteger();
            AtomicInteger objectRequests = new AtomicInteger();

            String manifestJson = JsonProvider.getManifestJsonWithDownloadableClientAndAssets(
                    client,
                    index,
                    assetObject
            );

            httpRoutes.createJsonContext(server, "/manifest.json", manifestJson);
            httpRoutes.createOctetStreamContext(
                    server,
                    "/client.jar",
                    clientRequests,
                    clientBytes
            );
            httpRoutes.createOctetStreamContext(
                    server,
                    "/assets/indexes/17.json",
                    indexRequests,
                    indexBytes
            );
            httpRoutes.createOctetStreamContext(
                    server,
                    "/" + objectRelativePath,
                    objectRequests,
                    objectBytes
            );
            server.start();

            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = FakeJavaExecutableFixture.create(
                    tempDir,
                    gameStartedMarker
            );

            LauncherConfiguration configuration = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                    new ResolvedLauncherConfiguration(
                            configuration,
                            ManifestSourceKind.EXPLICIT_URI
                    );

            LauncherEngine engine = new DefaultApplicationAssembly(resolvedLauncherConfiguration)
                    .createEngine();

            Path gameDirectory = new LocalDirectoryProvider(configuration)
                    .directories()
                    .game();

            //when
            LaunchResult result = engine.launch(configuration);

            //then
            assertTrue(result.success());
            assertEquals(LauncherState.RUNNING, result.finalState());
            assertArrayEquals(
                    clientBytes,
                    Files.readAllBytes(gameDirectory.resolve(client.path()))
            );
            assertArrayEquals(
                    indexBytes,
                    Files.readAllBytes(gameDirectory.resolve(index.path()))
            );
            assertArrayEquals(
                    objectBytes,
                    Files.readAllBytes(gameDirectory.resolve(assetObject.path()))
            );

            assertEquals(1, clientRequests.get());
            assertEquals(1, indexRequests.get());
            assertEquals(1, objectRequests.get());

            waitUntilExists(gameStartedMarker);
        }
    }

    @Test
    void should_launch_with_downloaded_client_artifact_when_manifest_declares_it(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        String clientPath = "versions/1.21.1/client.jar";
        byte[] clientBytes = "test-client-artifact".getBytes(StandardCharsets.UTF_8);
        String sha256 = sha256(clientBytes);
        AtomicInteger resourceRequests = new AtomicInteger();

        try (LocalHttpServer server = LocalHttpServer.bind()) {
            String clientUrl = server.uri("/client.jar").toString();
            String manifestJson = JsonProvider.getManifestJsonWithDownloadableClientArtifact(
                    clientPath,
                    sha256,
                    clientBytes.length,
                    clientUrl
            );

            httpRoutes.createJsonContext(server, "/manifest.json", manifestJson);
            httpRoutes.createOctetStreamContext(
                    server,
                    "/client.jar",
                    resourceRequests,
                    clientBytes
            );

            server.start();

            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = FakeJavaExecutableFixture.create(tempDir, gameStartedMarker);

            URI manifestUri = server.uri("/manifest.json");

            LauncherConfiguration configuration = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                    new ResolvedLauncherConfiguration(
                            configuration,
                            ManifestSourceKind.EXPLICIT_URI
                    );

            LauncherEngine engine =
                    new DefaultApplicationAssembly(resolvedLauncherConfiguration).createEngine();

            //when
            LaunchResult result = engine.launch(configuration);

            //then
            assertTrue(result.success());
            assertEquals(LauncherState.RUNNING, result.finalState());

            Path downloadedClient = new LocalDirectoryProvider(configuration)
                    .directories()
                    .game()
                    .resolve(clientPath);

            assertArrayEquals(clientBytes, Files.readAllBytes(downloadedClient));
            assertEquals(1, resourceRequests.get());
            waitUntilExists(gameStartedMarker);
        }
    }

    @Test
    void should_deliver_ordered_phases_before_completion_when_launcher_lifecycle_succeeds(@TempDir Path tempDir)
            throws IOException, InterruptedException {
        //given
        String path = "/manifest.json";

        try (LocalHttpServer server = LocalHttpServer.bind()) {
            httpRoutes.createJsonContext(server, path, JsonProvider.MANIFEST_JSON);
            server.start();

            URI manifestUri = server.uri(path);
            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = FakeJavaExecutableFixture.create(tempDir, gameStartedMarker);

            LauncherConfiguration config = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                    new ResolvedLauncherConfiguration(
                            config,
                            ManifestSourceKind.EXPLICIT_URI
                    );

            List<Object> callbacks = new CopyOnWriteArrayList<>();
            AtomicReference<PresentationLaunchCompletion> receivedCompletion =
                    new AtomicReference<>();

            CountDownLatch completionHandled = new CountDownLatch(1);

            Bootstrap bootstrap = new Bootstrap(resolvedLauncherConfiguration);

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
        }
    }

    @Test
    void should_return_success_result_when_launcher_lifecycle_completed_through_application_assembly(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        String path = "/manifest.json";

        try (LocalHttpServer server = LocalHttpServer.bind()) {
            httpRoutes.createJsonContext(server, path, JsonProvider.MANIFEST_JSON);
            server.start();

            URI manifestUri = server.uri(path);
            Path gameStartedMarker = tempDir.resolve("gamestarted.marker");
            Path executable = FakeJavaExecutableFixture.create(tempDir, gameStartedMarker);

            LauncherConfiguration config = new LauncherConfiguration(
                    manifestUri,
                    tempDir.resolve("launcher-directory"),
                    Optional.of(executable.toString())
            );

            ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                    new ResolvedLauncherConfiguration(
                            config,
                            ManifestSourceKind.EXPLICIT_URI
                    );

            DefaultApplicationAssembly assembly = new DefaultApplicationAssembly(resolvedLauncherConfiguration);

            LauncherEngine engine = assembly.createEngine();

            //when
            LaunchResult result = engine.launch(config);

            //then
            assertTrue(result.success());
            assertEquals(LauncherState.RUNNING, result.finalState());
            waitUntilExists(gameStartedMarker);
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

    private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes)
        );
    }
}
