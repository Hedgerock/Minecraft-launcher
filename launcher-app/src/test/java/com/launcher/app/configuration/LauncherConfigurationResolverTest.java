package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherConfigurationResolverTest {

    @Test
    void should_use_launcher_directory_from_second_argument() {
        //given
        String[] args = {
                "https://test-value-for-manifest:8080/manifest.json",
                "test-value-for-launcher-directory"
        };

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        Path launcherDirectory = configuration.launcherDirectory();

        assertEquals(
                "https://test-value-for-manifest:8080/manifest.json",
                configuration.manifestUri().toString()
        );

        assertEquals(
                Path.of("test-value-for-launcher-directory"),
                launcherDirectory
        );
    }

    @Test
    void should_use_manifest_uri_from_first_argument() {
        //given
        String[] args = {"https://test-value-for-manifest:8080/manifest.json",};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        URI manifest = configuration.manifestUri();

        assertEquals(
                "https://test-value-for-manifest:8080/manifest.json",
                manifest.toString()
        );

        assertEquals(
                Path.of(""),
                configuration.launcherDirectory()
        );

    }

    @Test
    void should_use_default_configuration_when_no_args_are_provided() {
        //given
        String[] args = {};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        URI manifest = configuration.manifestUri();
        Path launcherDirectory = configuration.launcherDirectory();

        assertEquals(
                "https://localhost:8080/manifest.json",
                manifest.toString()
        );

        assertEquals(
                Path.of(""),
                launcherDirectory
        );

    }

}
