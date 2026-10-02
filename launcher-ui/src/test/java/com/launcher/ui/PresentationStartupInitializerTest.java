package com.launcher.ui;

import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.configuration.LauncherDirectoryConfigurationException;
import com.launcher.app.configuration.LocalManifestUriConfigurationException;
import com.launcher.app.configuration.ManifestSourceKind;
import com.launcher.app.configuration.ManifestUriConfigurationException;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.configuration.path.LauncherUserPathsResolutionException;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.startup.PresentationStartupResult;
import com.launcher.ui.startup.PresentationStartupState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationStartupInitializerTest {
    private final PresentationStartupInitializer initializer = new PresentationStartupInitializer();

    @TempDir
    Path tempDir;

    @Test
    void should_keep_retry_available_when_local_configuration_still_fails() {
        //given
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger boundaryInitializations = new AtomicInteger();

        Supplier<ResolvedLauncherConfiguration> configurationResolver = () -> {
            attempts.incrementAndGet();

            throw new LocalManifestUriConfigurationException(
                    "Local configuration failed",
                    new ManifestUriConfigurationException("Invalid manifest URI")
            );
        };

        Consumer<ResolvedLauncherConfiguration> boundaryInitializer =
                configuration -> boundaryInitializations.incrementAndGet();

        //when
        PresentationStartupResult firstResult = initializer.initialize(
                configurationResolver,
                boundaryInitializer
        );

        PresentationStartupResult retryResult = initializer.initialize(
                configurationResolver,
                boundaryInitializer
        );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, firstResult.state());
        assertTrue(firstResult.retryAvailable());

        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, retryResult.state());
        assertTrue(retryResult.retryAvailable());

        assertEquals(2, attempts.get());
        assertEquals(0, boundaryInitializations.get());
    }

    @Test
    void should_initialize_boundary_after_local_configuration_is_fixed() {
        //given
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger boundaryInitializations = new AtomicInteger();

        Supplier<ResolvedLauncherConfiguration> configurationResolver = () -> {
            if (attempts.getAndIncrement() == 0) {
                throw new LocalManifestUriConfigurationException(
                        "Local configuration failed",
                        new ManifestUriConfigurationException("Invalid manifest URI")
                );
            }

            return new ResolvedLauncherConfiguration(getConfiguration(), ManifestSourceKind.LOCAL_CONFIG);
        };

        Consumer<ResolvedLauncherConfiguration> boundaryInitializer =
                configuration -> boundaryInitializations.incrementAndGet();

        //when
        PresentationStartupResult firstResult = initializer.initialize(
                configurationResolver,
                boundaryInitializer
        );

        PresentationStartupResult retryResult = initializer.initialize(
                configurationResolver,
                boundaryInitializer
        );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, firstResult.state());
        assertTrue(firstResult.retryAvailable());

        assertEquals(PresentationStartupState.AVAILABLE, retryResult.state());
        assertFalse(retryResult.retryAvailable());

        assertEquals(2, attempts.get());
        assertEquals(1, boundaryInitializations.get());
    }

    @Test
    void should_propagate_exception_when_configuration_resolver_failed() {
        //given
        RuntimeException configurationFailure = new RuntimeException(
                "Configuration resolver failed"
        );

        Supplier<ResolvedLauncherConfiguration> configurationResolver = () -> {
            throw configurationFailure;
        };

        Consumer<ResolvedLauncherConfiguration> boundaryInitializer = config -> {};

        //when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> initializer.initialize(
                        configurationResolver,
                        boundaryInitializer
                )
        );

        assertSame(configurationFailure, exception);
    }

    @Test
    void should_propagate_exception_when_boundary_initializer_failed() {
        //given
        LauncherConfiguration configuration = getConfiguration();

        Supplier<ResolvedLauncherConfiguration> configurationResolver = () ->
                new ResolvedLauncherConfiguration(configuration, ManifestSourceKind.LOCAL_CONFIG);

        RuntimeException boundaryFailure = new RuntimeException(
                "Boundary initialization failed"
        );

        Consumer<ResolvedLauncherConfiguration> boundaryInitializer = config -> {
            throw boundaryFailure;
        };

        //when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> initializer.initialize(
                        configurationResolver,
                        boundaryInitializer
                )
        );

        assertSame(boundaryFailure, exception);
    }

    @Test
    void should_reject_null_resolved_launcher_configuration() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> initializer.initialize(
                        () -> null,
                        configuration -> {}
                )
        );

        assertEquals("resolvedLauncherConfiguration", exception.getMessage());
    }

    @Test
    void should_return_configuration_failed_for_invalid_uri_argument() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        Supplier<ResolvedLauncherConfiguration> failingSupplier = () -> {
            String[] args = {"ftp://example.org/manifest.jar", tempDir.toString()};
            return new LauncherConfigurationResolver().resolve(args);
        };

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        failingSupplier,
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, result.state());
        assertFalse(result.retryAvailable());
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_configuration_failed_when_local_manifest_configuration_fails() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        () -> {
                            throw new LocalManifestUriConfigurationException(
                                    "test failure",
                                    new ManifestUriConfigurationException("Something went wrong")
                            );
                        },
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, result.state());
        assertTrue(result.retryAvailable());
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_configuration_failed_when_explicit_launcher_directory_path_failed() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        () -> {
                            throw new LauncherDirectoryConfigurationException("test failure");
                        },
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, result.state());
        assertFalse(result.retryAvailable());
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_configuration_failed_when_user_paths_resolution_fails() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        () -> {
                            throw new LauncherUserPathsResolutionException("test failure");
                        },
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, result.state());
        assertFalse(result.retryAvailable());
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_configuration_failed_for_failing_configuration_resolver_manifest_uri() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        () -> {
                            throw new ManifestUriConfigurationException("test failure");
                        },
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, result.state());
        assertFalse(result.retryAvailable());
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_available_state_for_success_configuration_resolver() {
        //given
        AtomicReference<ResolvedLauncherConfiguration> configurationAtomicReference = new AtomicReference<>();
        ResolvedLauncherConfiguration expectedResolvedConfiguration =
                new ResolvedLauncherConfiguration(getConfiguration(), ManifestSourceKind.LOCAL_CONFIG);

        //when
        PresentationStartupResult result = initializer
                .initialize(
                        () -> expectedResolvedConfiguration,
                        configurationAtomicReference::set
                );

        //then
        assertEquals(PresentationStartupState.AVAILABLE, result.state());
        assertFalse(result.retryAvailable());
        assertSame(expectedResolvedConfiguration, configurationAtomicReference.get());
    }

    @Test
    void should_reject_null_boundary_initializer() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> initializer.initialize(
                        () ->
                                new ResolvedLauncherConfiguration(
                                        getConfiguration(),
                                        ManifestSourceKind.LOCAL_CONFIG
                                ),
                        null
                )
        );

        assertEquals("boundaryInitializer", exception.getMessage());
    }

    @Test
    void should_reject_null_configuration_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> initializer.initialize(null, configuration -> {})
        );

        assertEquals("configurationResolver", exception.getMessage());
    }

    private LauncherConfiguration getConfiguration() {
        return new LauncherConfiguration(
                URI.create("https://localhost:8080/service"),
                tempDir
        );
    }
}
