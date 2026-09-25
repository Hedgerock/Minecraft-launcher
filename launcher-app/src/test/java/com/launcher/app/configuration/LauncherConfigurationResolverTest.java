package com.launcher.app.configuration;

import com.launcher.app.configuration.path.LauncherUserPaths;
import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void should_use_manifest_uri_from_first_argument(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        String[] args = {"https://test-value-for-manifest:8080/manifest.json"};

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        URI manifest = configuration.manifestUri();

        assertEquals(
                "https://test-value-for-manifest:8080/manifest.json",
                manifest.toString()
        );

        assertEquals(
                userPaths.defaultLauncherDirectory(),
                configuration.launcherDirectory()
        );
    }

    @Test
    void should_throw_when_configuration_file_not_exists(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        String[] args = {};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertInstanceOf(
                NoSuchFileException.class,
                exception.getCause()
        );
    }

    @Test
    void should_not_request_user_paths_when_manifest_uri_and_launcher_directory_are_provided() {
        //given
        Supplier<LauncherUserPaths> failingSupplier = () -> {
            throw new AssertionError("User paths must not be requested");
        };

        LauncherConfigurationResolver resolver =
                new LauncherConfigurationResolver(failingSupplier);

        String[] args = {
                "https://keystone.com/manifest.json",
                "keystone"
        };

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        assertEquals(
                URI.create("https://keystone.com/manifest.json"),
                configuration.manifestUri()
        );

        assertEquals(
                Path.of("keystone"),
                configuration.launcherDirectory()
        );
    }

    @Test
    void should_use_default_configuration_when_no_args_are_provided(@TempDir Path tempDir) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        Files.writeString(
                userPaths.configurationFile(),
                "manifest.uri=https://keystone.com/manifest.json",
                StandardCharsets.UTF_8
        );

        String[] args = {};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when
        LauncherConfiguration configuration = resolver.resolve(args);

        //then
        URI manifest = configuration.manifestUri();
        Path launcherDirectory = configuration.launcherDirectory();

        assertEquals(
                "https://keystone.com/manifest.json",
                manifest.toString()
        );

        assertEquals(
                userPaths.defaultLauncherDirectory(),
                launcherDirectory
        );
    }
}
