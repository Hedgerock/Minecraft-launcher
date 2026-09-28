package com.launcher.ui;

import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.configuration.ManifestUriConfigurationException;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.startup.PresentationStartupState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationStartupInitializerTest {
    private final PresentationStartupInitializer initializer = new PresentationStartupInitializer();

    @TempDir
    Path tempDir;

    @Test
    void should_propagate_exception_when_boundary_initializer_failed() {
        //given
        LauncherConfiguration configuration = getConfiguration();

        Supplier<LauncherConfiguration> configurationResolver = () -> configuration;

        RuntimeException boundaryFailure = new RuntimeException(
                "Boundary initialization failed"
        );

        Consumer<LauncherConfiguration> boundaryInitializer = config -> {
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
    void should_reject_null_configuration() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> initializer.initialize(
                        () -> null,
                        configuration -> {}
                )
        );

        assertEquals("configuration", exception.getMessage());
    }

    @Test
    void should_return_configuration_failed_for_invalid_uri_argument() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        Supplier<LauncherConfiguration> failingSupplier = () -> {
            String[] args = {"ftp://example.org/manifest.jar", tempDir.toString()};
            return new LauncherConfigurationResolver().resolve(args);
        };

        //when
        PresentationStartupState startupState = initializer
                .initialize(
                        failingSupplier,
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, startupState);
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_configuration_failed_for_failing_configuration_resolver() {
        //given
        AtomicBoolean boundaryInitialized = new AtomicBoolean();

        //when
        PresentationStartupState startupState = initializer
                .initialize(
                        () -> {
                            throw new ManifestUriConfigurationException("test failure");
                        },
                        configuration -> boundaryInitialized.set(true)
                );

        //then
        assertEquals(PresentationStartupState.CONFIGURATION_FAILED, startupState);
        assertFalse(boundaryInitialized.get());
    }

    @Test
    void should_return_available_state_for_success_configuration_resolver() {
        //given
        AtomicReference<LauncherConfiguration> configurationAtomicReference = new AtomicReference<>();
        LauncherConfiguration expectedConfiguration = getConfiguration();

        //when
        PresentationStartupState startupState = initializer
                .initialize(
                        () -> expectedConfiguration,
                        configurationAtomicReference::set
                );

        //then
        assertEquals(PresentationStartupState.AVAILABLE, startupState);
        assertSame(expectedConfiguration, configurationAtomicReference.get());
    }

    @Test
    void should_reject_null_boundary_initializer() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> initializer.initialize(
                        this::getConfiguration,
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
