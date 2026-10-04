package com.launcher.app.service.factory;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.api.manifest.service.SignedHttpManifestService;
import com.launcher.app.configuration.ManagedManifestUris;
import com.launcher.app.configuration.ManifestPublicKeyConfigurationException;
import com.launcher.app.configuration.ManifestSourceKind;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.app.service.LauncherServices;
import com.launcher.app.storage.directory.LocalDirectoryProvider;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.RuntimeEnvironmentProvider;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.storage.file.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultLauncherServiceFactoryTest {

    @TempDir
    Path tempDir;

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
}
