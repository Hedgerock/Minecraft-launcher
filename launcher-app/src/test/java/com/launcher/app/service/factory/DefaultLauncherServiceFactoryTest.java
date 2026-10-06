package com.launcher.app.service.factory;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.http.LauncherHttpClient;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.api.manifest.service.SignedHttpManifestService;
import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import com.launcher.app.configuration.ManagedManifestUris;
import com.launcher.app.configuration.ManifestPublicKeyConfigurationException;
import com.launcher.app.configuration.ManifestSourceKind;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.app.service.LauncherServices;
import com.launcher.app.storage.directory.LocalDirectoryProvider;
import com.launcher.app.support.JsonProvider;
import com.launcher.app.support.StubLauncherHttpClient;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.event.EventBus;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.RuntimeEnvironmentProvider;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.storage.file.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultLauncherServiceFactoryTest {
    private static final String ALGORITHM = "Ed25519";

    private static final URI MANIFEST_URI =
            URI.create("https://example.test/manifest.json");
    private static final URI SIGNATURE_URI =
            URI.create("https://example.test/manifest.sig");

    @TempDir
    Path tempDir;

    @Test
    void should_preserve_file_download_for_local_config_source_kind() throws Exception {
        //given
        Path source = tempDir.resolve("source.jar");
        Files.writeString(source, "resource-content");

        ResourceEntry resource = new ResourceEntry(
                "mods/test.jar",
                "test-sha256",
                Files.size(source),
                source.toUri().toString()
        );

        LauncherServices services = launcherServiceFactory(
                () -> {
                    throw new AssertionError(
                            "Public key must not be loaded"
                    );
                },
                ManifestSourceKind.LOCAL_CONFIG
        ).createServices();

        //when
        services.downloadService().download(
                new DownloadPlan(
                        List.of(resource)
                )
        );

        //then
        assertEquals(
                "resource-content",
                Files.readString(tempDir.resolve("game/mods/test.jar")
                )
        );
    }

    @Test
    void should_reject_file_resource_for_managed_source_kind() throws Exception {
        //given
        Path source = tempDir.resolve("source.jar");
        Files.writeString(source, "resource-content");

        ResourceEntry resourceEntry = new ResourceEntry(
                "mods/test.jar",
                "test-sha256",
                Files.size(source),
                source.toUri().toString()
        );

        PublicKey publicKey = generateKey();
        LauncherServices services = launcherServiceFactory(
                () -> publicKey,
                ManifestSourceKind.MANAGED
        ).createServices();

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> services.downloadService().download(
                        new DownloadPlan(List.of(resourceEntry))
                )
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(
                "Managed resource requires an HTTPS URI",
                exception.getCause().getMessage()
        );
        assertFalse(Files.exists(tempDir.resolve(
                "game/mods/test.jar"
        )));
    }

    @Test
    void should_fail_when_manifest_was_modified_after_signing() throws Exception {
        //given
        byte[] manifestBytes = JsonProvider.MANIFEST_JSON
                .getBytes(StandardCharsets.UTF_8);

        KeyPair keyPair = KeyPairGenerator
                .getInstance(ALGORITHM)
                .generateKeyPair();

        byte[] signatureBytes =
                signatureBytes(keyPair, manifestBytes);

        manifestBytes[0] ^= 1;

        ManifestService manifestService = getManifestService(manifestBytes, signatureBytes, keyPair);

        //when & then
        ManifestSignatureVerificationException exception = assertThrows(
                ManifestSignatureVerificationException.class,
                manifestService::loadManifest
        );

        assertEquals(
               "Manifest signature is invalid",
               exception.getMessage()
        );
    }

    @Test
    void should_load_managed_manifest_through_signed_manifest_service() throws Exception {
        //given
        byte[] manifestBytes = JsonProvider.MANIFEST_JSON
                .getBytes(StandardCharsets.UTF_8);

        KeyPair keyPair = KeyPairGenerator
                .getInstance(ALGORITHM)
                .generateKeyPair();

        byte[] signatureBytes =
                signatureBytes(keyPair, manifestBytes);

        //when
        ManifestService manifestService = getManifestService(manifestBytes, signatureBytes, keyPair);
        ManifestLoadResult result =
                manifestService.loadManifest();

        //then
        assertInstanceOf(
                SignedHttpManifestService.class,
                manifestService
        );

        Manifest manifest = result.manifest();

        assertEquals(
                "1.12.2",
                manifest.minecraftVersion()
        );

        assertEquals(
                "fabric",
                manifest.loader().type()
        );

        assertEquals(
                "0.16.10",
                manifest.loader().version()
        );

        assertEquals(
                "net.minecraft.client.main.Main",
                manifest.launchInfo().mainClass()
        );
    }

    @Test
    void should_fail_when_public_key_is_not_loaded() {
        //given
        Supplier<PublicKey> failingKeySupplier = () -> {
            throw new ManifestPublicKeyConfigurationException("Failed to load publicKey");
        };

        DefaultLauncherServiceFactory managedFactory = launcherServiceFactory(
                failingKeySupplier,
                ManifestSourceKind.MANAGED
        );

        //when & then
        assertThrows(
                ManifestPublicKeyConfigurationException.class,
                managedFactory::createServices
        );
    }

    @Test
    void should_return_http_manifest_service_for_explicit_uri_and_local_config_manifest_source_kind() {
        //given
        Supplier<PublicKey> assertionErrorSupplier = () -> {
            throw new AssertionError("Failed to load publicKey");
        };

        DefaultLauncherServiceFactory localFactory = launcherServiceFactory(
                assertionErrorSupplier,
                ManifestSourceKind.LOCAL_CONFIG
        );

        DefaultLauncherServiceFactory explicitFactory = launcherServiceFactory(
                assertionErrorSupplier,
                ManifestSourceKind.EXPLICIT_URI
        );

        //when
        LauncherServices localService = localFactory.createServices();
        LauncherServices explicitService = explicitFactory.createServices();

        //then
        assertInstanceOf(
                HttpManifestService.class,
                localService.manifestService()
        );

        assertInstanceOf(
                HttpManifestService.class,
                explicitService.manifestService()
        );
    }

    @Test
    void should_return_signed_http_manifest_service_for_managed_manifest_source_kind() {
        //given
        AtomicInteger totalCalls = new AtomicInteger(0);
        DefaultLauncherServiceFactory defaultLauncherServiceFactory = launcherServiceFactory(
                publicKeySupplier(totalCalls),
                ManifestSourceKind.MANAGED
        );

        //when
        LauncherServices services = defaultLauncherServiceFactory.createServices();

        //then
        assertInstanceOf(
                SignedHttpManifestService.class,
                services.manifestService()
        );

        assertEquals(
                1,
                totalCalls.get()
        );
    }

    @Test
    void should_reject_null_public_key_supplier_value() {
        //given
        DefaultLauncherServiceFactory defaultLauncherServiceFactory = new DefaultLauncherServiceFactory(
                resolvedLauncherConfiguration(ManifestSourceKind.MANAGED),
                launcherInfrastructure(),
                resourcePathResolver(),
                directoryProvider(),
                runtimeEnvironmentProvider(),
                () -> null
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                defaultLauncherServiceFactory::createServices
        );

        assertEquals(
                "publicKey",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_public_key_supplier() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                resolvedLauncherConfiguration(),
                                launcherInfrastructure(),
                                resourcePathResolver(),
                                directoryProvider(),
                                runtimeEnvironmentProvider(),
                                null
                        )
                );

        assertEquals(
                "publicKeySupplier",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_environment_provider() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                resolvedLauncherConfiguration(),
                                launcherInfrastructure(),
                                resourcePathResolver(),
                                directoryProvider(),
                                null
                        )
                );

        assertEquals(
                "environmentProvider",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_directory_provider() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                resolvedLauncherConfiguration(),
                                launcherInfrastructure(),
                                resourcePathResolver(),
                                null,
                                runtimeEnvironmentProvider()
                        )
                );

        assertEquals(
                "directoryProvider",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_resource_path_resolver() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                resolvedLauncherConfiguration(),
                                launcherInfrastructure(),
                                null,
                                directoryProvider(),
                                runtimeEnvironmentProvider()
                        )
                );

        assertEquals(
                "resourcePathResolver",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_launcher_infrastructure() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                resolvedLauncherConfiguration(),
                                null,
                                resourcePathResolver(),
                                directoryProvider(),
                                runtimeEnvironmentProvider()
                        )
                );

        assertEquals(
                "infrastructure",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_resolved_launcher_configuration() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultLauncherServiceFactory(
                                null,
                                launcherInfrastructure(),
                                resourcePathResolver(),
                                directoryProvider(),
                                runtimeEnvironmentProvider()
                        )
                );

        assertEquals(
                "resolvedLauncherConfiguration",
                exception.getMessage()
        );
    }

    private RuntimeEnvironmentProvider runtimeEnvironmentProvider() {
        return new SystemRuntimeEnvironmentProvider();
    }

    private DirectoryProvider directoryProvider() {
        return new LocalDirectoryProvider(
                configuration()
        );
    }

    private ResourcePathResolver resourcePathResolver() {
        return new SafeResourcePathResolver();
    }

    private LauncherInfrastructure launcherInfrastructure() {
        return new LauncherInfrastructure(
                new JavaLauncherHttpClient(),
                new LocalFileStorage(),
                new EventBus()
        );
    }

    private ResolvedLauncherConfiguration resolvedLauncherConfiguration() {
        return resolvedLauncherConfiguration(
                ManifestSourceKind.EXPLICIT_URI
        );
    }

    private ResolvedLauncherConfiguration resolvedLauncherConfiguration(
            ManifestSourceKind sourceKind
    ) {
        return switch (sourceKind) {
            case EXPLICIT_URI, LOCAL_CONFIG -> new ResolvedLauncherConfiguration(
                    configuration(),
                    sourceKind
            );
            case MANAGED -> {
                LauncherConfiguration configuration = configuration();

                yield new ResolvedLauncherConfiguration(
                        configuration(),
                        sourceKind,
                        Optional.of(
                                new ManagedManifestUris(
                                        configuration.manifestUri(),
                                        URI.create("https://example.com/manifest.sig")
                                )
                        )
                );
            }
        };
    }

    private LauncherConfiguration configuration() {
        return new LauncherConfiguration(
                URI.create("https://example.com/manifest.json"),
                tempDir
        );
    }

    private PublicKey generateKey() throws Exception {
        KeyPair keyPair =
                KeyPairGenerator.getInstance("Ed25519").generateKeyPair();

        return keyPair.getPublic();
    }

    private Supplier<PublicKey> publicKeySupplier(
            AtomicInteger timesCalled
    ) {
        return () -> {
            try {
                timesCalled.incrementAndGet();
                return generateKey();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private DefaultLauncherServiceFactory launcherServiceFactory(
            Supplier<PublicKey> publicKeySupplier,
            ManifestSourceKind sourceKind
    ) {
        return new DefaultLauncherServiceFactory(
                resolvedLauncherConfiguration(sourceKind),
                launcherInfrastructure(),
                resourcePathResolver(),
                directoryProvider(),
                runtimeEnvironmentProvider(),
                publicKeySupplier
        );
    }

    private byte[] signatureBytes(KeyPair keyPair, byte[] manifestBytes) throws Exception {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(keyPair.getPrivate());
        signer.update(manifestBytes);

        return signer.sign();
    }

    private ManifestService getManifestService(
            byte[] manifestBytes,
            byte[] signatureBytes,
            KeyPair keyPair
    ) {
        LauncherHttpClient httpClient =
                new StubLauncherHttpClient(
                        MANIFEST_URI,
                        manifestBytes,
                        SIGNATURE_URI,
                        signatureBytes
                );

        LauncherConfiguration configuration =
                new LauncherConfiguration(
                        MANIFEST_URI,
                        tempDir.resolve("launch-directory")
                );

        ResolvedLauncherConfiguration resolvedLauncherConfiguration =
                new ResolvedLauncherConfiguration(
                        configuration,
                        ManifestSourceKind.MANAGED,
                        Optional.of(
                                new ManagedManifestUris(
                                        MANIFEST_URI,
                                        SIGNATURE_URI
                                )
                        )
                );

        LauncherInfrastructure infrastructure =
                new LauncherInfrastructure(
                        httpClient,
                        new LocalFileStorage(),
                        new EventBus()
                );

        DefaultLauncherServiceFactory factory =
                new DefaultLauncherServiceFactory(
                        resolvedLauncherConfiguration,
                        infrastructure,
                        new SafeResourcePathResolver(),
                        new LocalDirectoryProvider(configuration),
                        new SystemRuntimeEnvironmentProvider(),
                        keyPair::getPublic
                );

        return factory.createServices().manifestService();
    }
}
