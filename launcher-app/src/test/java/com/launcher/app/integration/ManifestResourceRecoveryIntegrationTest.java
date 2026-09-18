package com.launcher.app.integration;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.app.service.LauncherServices;
import com.launcher.app.service.factory.DefaultLauncherServiceFactory;
import com.launcher.app.storage.directory.LocalDirectoryProvider;
import com.launcher.app.support.JsonProvider;
import com.launcher.app.support.LocalServerStarter;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.resource.ResourceSetConflictException;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.storage.file.LocalFileStorage;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManifestResourceRecoveryIntegrationTest {
    private final LocalServerStarter localServerStarter = new LocalServerStarter();

    @Test
    void should_reject_entire_download_plan_when_resource_targets_conflict(@TempDir Path tempDir)
            throws NoSuchAlgorithmException, IOException {
        //given
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        AtomicInteger resourceRequests = new AtomicInteger();

        byte[] content = "example-resource".getBytes(StandardCharsets.UTF_8);

        String sha256 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(content));

        String resourceUrl = baseUrl + "/files/mods/example.jar";

        localServerStarter.createOctetStreamContext(
                server,
                "/files/mods/example.jar",
                resourceRequests,
                content
        );

        server.start();

        try {
            LauncherConfiguration configuration = new LauncherConfiguration(
                    URI.create(baseUrl + "/manifest.json"),
                    tempDir.resolve("launch-directory")
            );

            DirectoryProvider directoryProvider = new LocalDirectoryProvider(configuration);
            DefaultLauncherServiceFactory defaultLauncherServiceFactory = getFactory(configuration, directoryProvider);
            LauncherServices services = defaultLauncherServiceFactory.createServices();

            ResourceEntry ordinaryResource = new ResourceEntry(
                    "mods/ordinary.jar",
                    sha256,
                    content.length,
                    resourceUrl
            );

            ResourceEntry firstResource = new ResourceEntry(
                    "mods/example.jar",
                    sha256,
                    content.length,
                    resourceUrl
            );

            ResourceEntry conflictingResource = new ResourceEntry(
                    "mods/./example.jar",
                    sha256,
                    content.length + 1,
                    resourceUrl
            );

            DownloadPlan downloadPlan = new DownloadPlan(
                    List.of(
                            ordinaryResource,
                            firstResource,
                            conflictingResource
                    )
            );

            //when & then
            ResourceSetConflictException exception = assertThrows(
                    ResourceSetConflictException.class,
                    () -> services.downloadService().download(downloadPlan)
            );

            Path gameDirectory = directoryProvider.directories().game();
            assertFalse(Files.exists(gameDirectory.resolve("mods/ordinary.jar")));
            assertFalse(Files.exists(gameDirectory.resolve("mods/example.jar")));
            assertEquals(0, resourceRequests.get());

            Path targetPath = gameDirectory.resolve("mods/example.jar");

            assertEquals(
                    "Conflicting resources for target '%s': '%s' and '%s'".formatted(
                            targetPath,
                            "mods/example.jar",
                            "mods/./example.jar"
                    ),
                    exception.getMessage()
            );

            assertEquals(
                    targetPath,
                    exception.getTargetPath()
            );

            assertEquals(
                    firstResource,
                    exception.getFirstResource()
            );

            assertEquals(
                    conflictingResource,
                    exception.getConflictingResource()
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void should_recover_compatible_resources_when_paths_resolve_to_same_target(
            @TempDir Path tempDir
    ) throws IOException, NoSuchAlgorithmException {
        //given
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        AtomicInteger resourceRequests = new AtomicInteger();

        byte[] content = "example-resource".getBytes(StandardCharsets.UTF_8);

        String sha256 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(content));

        String resourceUrl = baseUrl + "/files/mods/example.jar";

        String manifestJson = JsonProvider.getManifestJsonWithSameFileAndAsset(
                resourceUrl,
                sha256,
                content.length
        );

        localServerStarter.createOctetStreamContext(
                server,
                "/files/mods/example.jar",
                resourceRequests,
                content
        );

        localServerStarter.createJsonContext(server, "/manifest.json", manifestJson);

        server.start();

        try {
            LauncherConfiguration configuration = new LauncherConfiguration(
                    URI.create(baseUrl + "/manifest.json"),
                    tempDir.resolve("launch-directory")
            );

            DirectoryProvider directoryProvider = new LocalDirectoryProvider(configuration);
            DefaultLauncherServiceFactory defaultLauncherServiceFactory = getFactory(configuration, directoryProvider);
            LauncherServices services = defaultLauncherServiceFactory.createServices();

            ManifestLoadResult result = services.manifestService().loadManifest();
            Manifest manifest = result.manifest();

            //when
            VerificationPlan initialPlan = services.verificationService().verify(manifest);
            DownloadPlan downloadPlan = new DownloadPlanBuilder().build(initialPlan);

            services.downloadService().download(downloadPlan);
            VerificationPlan finalPlan = services.verificationService().verify(manifest);

            //then
            assertEquals(
                    1,
                    initialPlan.resources().size()
            );

            assertFalse(initialPlan.isValid());
            assertEquals(VerificationStatus.MISSING, initialPlan.resources().getFirst().status());
            assertEquals(1, downloadPlan.resources().size());
            assertEquals(1, resourceRequests.get());

            Path targetPath = directoryProvider.directories().game().resolve("mods/example.jar");
            assertArrayEquals(content, Files.readAllBytes(targetPath));

            assertEquals(
                    1,
                    finalPlan.resources().size()
            );

            assertTrue(finalPlan.isValid());
            assertEquals(
                    VerificationStatus.VALID,
                    finalPlan.resources().getFirst().status()
            );
        } finally {
            server.stop(0);
        }
    }

    private DefaultLauncherServiceFactory getFactory(
            LauncherConfiguration configuration,
            DirectoryProvider directoryProvider
    ) {
        return new DefaultLauncherServiceFactory(
                configuration,
                new LauncherInfrastructure(
                        new JavaLauncherHttpClient(),
                        new LocalFileStorage(),
                        new EventBus()
                ),
                new SafeResourcePathResolver(),
                directoryProvider,
                new SystemRuntimeEnvironmentProvider()
        );
    }
}
