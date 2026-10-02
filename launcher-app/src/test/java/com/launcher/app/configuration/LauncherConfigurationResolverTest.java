package com.launcher.app.configuration;

import com.launcher.app.configuration.path.LauncherUserPaths;
import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LauncherConfigurationResolverTest {

    @Test
    void should_return_different_manifest_source_kind_for_matching_uris(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        Files.writeString(
                userPaths.configurationFile(),
                "manifest.uri=https://local.example/manifest.json"
        );

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(new ByteArrayInputStream(
                        "manifest.uri=https://local.example/manifest.json"
                                .getBytes(StandardCharsets.UTF_8)
                ))
        );

        //when
        ResolvedLauncherConfiguration firstResult = resolver.resolve(new String[0]);
        ResolvedLauncherConfiguration secondResult = resolver.resolve(new String[]{"--local-config"});
        ResolvedLauncherConfiguration thirdResult = resolver.resolve(new String[]{
                "https://local.example/manifest.json"}
        );

        //then
        assertTheSameUri(
                "https://local.example/manifest.json",
                firstResult,
                secondResult,
                thirdResult
        );

        assertEquals(
                ManifestSourceKind.MANAGED,
                firstResult.sourceKind()
        );

        assertEquals(
                ManifestSourceKind.LOCAL_CONFIG,
                secondResult.sourceKind()
        );

        assertEquals(
                ManifestSourceKind.EXPLICIT_URI,
                thirdResult.sourceKind()
        );
    }

    @Test
    void should_return_manifest_uri_property_from_source(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        Files.writeString(
                userPaths.configurationFile(),
                "manifest.uri=https://local.example/manifest.json"
        );

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(new ByteArrayInputStream(
                        "manifest.uri=https://example.org/manifest.json"
                                .getBytes(StandardCharsets.UTF_8)
                ))
        );

        //when
        ResolvedLauncherConfiguration resolved = resolver.resolve(new String[0]);

        //then
        assertEquals(
                URI.create("https://example.org/manifest.json"),
                resolved.configuration().manifestUri()
        );

        assertEquals(
                ManifestSourceKind.MANAGED,
                resolved.sourceKind()
        );
    }

    @Test
    void should_fail_when_source_cannot_read_manifest_uri(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("Bad read");
                    }
                })
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(new String[0])
        );

        assertEquals(
                "Failed to read bundled manifest properties",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_source_has_missing_manifest_uri(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(new ByteArrayInputStream(
                        "manifest.name=keystone-name"
                                .getBytes(StandardCharsets.UTF_8)
                ))
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(new String[0])
        );

        assertEquals(
                "Manifest URI is not configured",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_source_has_unsupported_manifest_uri(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(new ByteArrayInputStream(
                        "manifest.uri=ftp://example.org/manifest.json"
                                .getBytes(StandardCharsets.UTF_8)
                ))
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(new String[0])
        );

        assertEquals(
                "Manifest URI must be an absolute HTTP(S) URI with a host",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_invalid_explicit_launcher_directory_value() {
        //given
        String[] args = {"https://example.org/manifest.json", "invalid\0path"};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when & then
        LauncherDirectoryConfigurationException exception = assertThrows(
                LauncherDirectoryConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertEquals("Invalid launcher directory path", exception.getMessage());
        assertInstanceOf(InvalidPathException.class, exception.getCause());
    }

    @Test
    void should_reject_empty_explicit_launcher_directory_value() {
        //given
        String[] args = {"https://example.org/manifest.json", ""};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when & then
        LauncherDirectoryConfigurationException exception = assertThrows(
                LauncherDirectoryConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertEquals("Launcher directory path cannot be blank", exception.getMessage());
    }

    @Test
    void should_reject_blank_explicit_launcher_directory_value() {
        //given
        String[] args = {"https://example.org/manifest.json", " "};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when & then
        LauncherDirectoryConfigurationException exception = assertThrows(
                LauncherDirectoryConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertEquals("Launcher directory path cannot be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_source(@TempDir Path tempDir) {
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherConfigurationResolver(
                        () -> userPaths,
                        null
                )
        );

        assertEquals("source", exception.getMessage());
    }

    @Test
    void should_reject_null_explicit_launcher_directory_value() {
        //given
        String[] args = {"https://example.org/manifest.json", null};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(args)
        );

        assertEquals("value", exception.getMessage());
    }

    @Test
    void should_reject_explicit_manifest_uri_before_resolving_user_paths() {
        //given
        Supplier<LauncherUserPaths> failingSupplier = () -> {
            throw new AssertionError("Something went wrong");
        };

        String[] args = {"ftp://example.org/manifest.json"};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(failingSupplier);

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );
    }

    @Test
    void should_throw_when_uri_argument_is_not_valid(@TempDir Path tempDir) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        String[] args = { "ftp://test-value-for-manifest:8080/manifest.json" };

        Files.writeString(
                userPaths.configurationFile(),
                "manifest.uri=https://keystone.com/manifest.json",
                StandardCharsets.UTF_8
        );

        LauncherConfigurationResolver resolver =
                new LauncherConfigurationResolver(() -> userPaths);

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );
    }

    @Test
    void should_use_launcher_directory_from_second_argument() {
        //given
        String[] args = {
                "https://test-value-for-manifest:8080/manifest.json",
                "test-value-for-launcher-directory"
        };

        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver();

        //when
        ResolvedLauncherConfiguration resolved = resolver.resolve(args);

        //then
        LauncherConfiguration configuration = resolved.configuration();
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
        ResolvedLauncherConfiguration resolved = resolver.resolve(args);

        //then
        LauncherConfiguration configuration = resolved.configuration();
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

        String[] args = {"--local-config"};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when & then
        LocalManifestUriConfigurationException exception = assertThrows(
                LocalManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );

        ManifestUriConfigurationException sourceFailure = assertInstanceOf(
                ManifestUriConfigurationException.class,
                exception.getCause()
        );

        assertInstanceOf(NoSuchFileException.class, sourceFailure.getCause());
    }

    @Test
    void should_wrap_invalid_manifest_uri_from_local_configuration(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        Files.writeString(
                userPaths.configurationFile(),
                "manifest.uri=ftp://example.org/manifest.json"
        );

        LauncherConfigurationResolver resolver =
                new LauncherConfigurationResolver(() -> userPaths);

        String[] args = {"--local-config"};

        //when & then
        LocalManifestUriConfigurationException exception = assertThrows(
                LocalManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertInstanceOf(
                ManifestUriConfigurationException.class,
                exception.getCause()
        );
    }

    @Test
    void should_use_local_manifest_uri_and_explicit_path_directory_when_configuration_provided(
            @TempDir Path tempDir
    ) throws IOException {
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

        Supplier<LauncherUserPaths> launcherUserPathsSupplier = () -> userPaths;

        LauncherConfigurationResolver resolver =
                new LauncherConfigurationResolver(launcherUserPathsSupplier);

        String[] args = {
                "--local-config",
                "game-directory"
        };

        //when
        ResolvedLauncherConfiguration resolved = resolver.resolve(args);

        //then
        LauncherConfiguration configuration = resolved.configuration();

        assertEquals(
                URI.create("https://keystone.com/manifest.json"),
                configuration.manifestUri()
        );

        assertEquals(
                Path.of("game-directory"),
                configuration.launcherDirectory()
        );

        assertEquals(
                ManifestSourceKind.LOCAL_CONFIG,
                resolved.sourceKind()
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
        ResolvedLauncherConfiguration resolved = resolver.resolve(args);

        //then
        LauncherConfiguration configuration = resolved.configuration();

        assertEquals(
                URI.create("https://keystone.com/manifest.json"),
                configuration.manifestUri()
        );

        assertEquals(
                Path.of("keystone"),
                configuration.launcherDirectory()
        );

        assertEquals(
                ManifestSourceKind.EXPLICIT_URI,
                resolved.sourceKind()
        );
    }

    @Test
    void should_throw_when_explicitly_selected_local_configuration_file_is_missing(@TempDir Path tempDir) {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        String[] args = {
                "--local-config",
                "game-directory"
        };
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when
        LocalManifestUriConfigurationException exception = assertThrows(
                LocalManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertEquals(
                "Local manifest URI configuration is unavailable",
                exception.getMessage()
        );

        Throwable reason = exception.getCause();

        assertInstanceOf(ManifestUriConfigurationException.class, reason);
        assertInstanceOf(NoSuchFileException.class, reason.getCause());
    }

    @Test
    void should_use_local_manifest_uri_when_local_configuration_is_explicitly_selected(
            @TempDir Path tempDir
    ) throws IOException {
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

        String[] args = {
                "--local-config"
        };
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths
        );

        //when
        ResolvedLauncherConfiguration resolved = resolver.resolve(args);

        //then
        LauncherConfiguration configuration = resolved.configuration();
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

        assertEquals(
                ManifestSourceKind.LOCAL_CONFIG,
                resolved.sourceKind()
        );
    }

    @Test
    void should_not_fallback_to_local_configuration_when_bundled_resource_is_missing(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        LauncherUserPaths userPaths = new LauncherUserPaths(
                tempDir.resolve("keystone.properties"),
                tempDir.resolve("keystone")
        );

        Files.writeString(
                userPaths.configurationFile(),
                "https://local-example.com/manifest.json"
        );

        String[] args = {};
        LauncherConfigurationResolver resolver = new LauncherConfigurationResolver(
                () -> userPaths,
                getSource(null)
        );

        //when
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> resolver.resolve(args)
        );

        assertEquals(
                "Bundled manifest properties are unavailable",
                exception.getMessage()
        );
    }

    private BundledManifestUriSource getSource(InputStream inputStream) {
        return new BundledManifestUriSource(
                new DefaultManifestUriParser(),
                () -> inputStream
        );
    }

    private void assertTheSameUri(
            String expectedUri,
            ResolvedLauncherConfiguration... resolvedLauncherConfigurations
    ) {
        Arrays.stream(resolvedLauncherConfigurations).forEach(resolved -> assertEquals(
                URI.create(expectedUri),
                resolved.configuration().manifestUri()
        ));
    }
}
