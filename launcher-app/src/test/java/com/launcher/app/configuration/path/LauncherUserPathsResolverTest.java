package com.launcher.app.configuration.path;

import com.launcher.model.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LauncherUserPathsResolverTest {

    @Test
    void should_reject_null_operating_system(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(),
                tempDir
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null)
        );

        assertEquals("operatingSystem", exception.getMessage());
    }

    @Test
    void should_reject_non_absolute_user_home_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LauncherUserPathsResolver(
                        Map.of(),
                        Path.of("relative")
                )
        );

        assertEquals("userHome must be absolute", exception.getMessage());
    }

    @Test
    void should_reject_null_user_home() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherUserPathsResolver(
                        Map.of(),
                        null
                )
        );

        assertEquals("userHome", exception.getMessage());
    }

    @Test
    void should_reject_environment_null_value(@TempDir Path tempDir) {
        //given
        Map<String, String> environment = new HashMap<>();

        environment.put("XDG_CONFIG_HOME", null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LauncherUserPathsResolver(
                        environment,
                        tempDir
                )
        );
    }

    @Test
    void should_reject_null_environment(@TempDir Path tempDir) {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherUserPathsResolver(
                        null,
                        tempDir
                )
        );

        assertEquals("environment", exception.getMessage());
    }

    @Test
    void should_use_library_application_support_for_macos(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.MACOS);

        //then
        Path expectedLauncherDirectory = tempDir
                .resolve("Library")
                .resolve("Application Support")
                .resolve("keystone");

        assertEquals(
                expectedLauncherDirectory,
                paths.defaultLauncherDirectory()
        );

        assertEquals(
                expectedLauncherDirectory.resolve("keystone.properties"),
                paths.configurationFile()
        );
    }

    @Test
    void should_use_fallback_when_xdg_config_home_and_data_home_is_missing(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.LINUX);

        //then
        assertEquals(
                tempDir
                        .resolve(".config")
                        .resolve("keystone")
                        .resolve("keystone.properties"),
                paths.configurationFile()
        );

        assertEquals(
                tempDir
                        .resolve(".local/share")
                        .resolve("keystone"),
                paths.defaultLauncherDirectory()
        );
    }

    @Test
    void should_use_fallback_when_xdg_data_home_is_relative(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "XDG_DATA_HOME", "relative/data"
                ),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.LINUX);

        //then
        assertEquals(
                tempDir
                        .resolve(".local/share")
                        .resolve("keystone"),
                paths.defaultLauncherDirectory()
        );
    }

    @Test
    void should_use_fallback_when_xdg_config_home_is_relative(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "XDG_CONFIG_HOME", "relative/config"
                ),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.LINUX);

        //then
        assertEquals(
                tempDir
                        .resolve(".config")
                        .resolve("keystone")
                        .resolve("keystone.properties"),
                paths.configurationFile()
        );
    }

    @Test
    void should_use_xdg_config_and_data_directories_for_linux(@TempDir Path tempDir) {
        //given
        Path configHome = tempDir.resolve("config");
        Path dataHome = tempDir.resolve("data");

        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "XDG_CONFIG_HOME", configHome.toString(),
                        "XDG_DATA_HOME", dataHome.toString()
                ),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.LINUX);

        //then
        Path configDirectory = configHome
                .resolve("keystone")
                .resolve("keystone.properties");

        assertEquals(
                configDirectory,
                paths.configurationFile()
        );

        assertEquals(
                dataHome.resolve("keystone"),
                paths.defaultLauncherDirectory()
        );
    }

    @Test
    void should_reject_non_absolute_path_for_windows(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "LOCALAPPDATA", "relative-path",
                        "APPDATA", tempDir.resolve("Roaming").toString()
                ),
                tempDir
        );

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(OperatingSystem.WINDOWS)
        );

        assertEquals(
                "LOCALAPPDATA must be absolute",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_missing_local_app_data_property_for_windows(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "APPDATA", tempDir.resolve("Roaming").toString()
                ),
                tempDir
        );

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(OperatingSystem.WINDOWS)
        );

        assertEquals(
                "LOCALAPPDATA is unavailable",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_blank_local_app_data_property_for_windows(@TempDir Path tempDir) {
        //given
        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "LOCALAPPDATA", " ",
                        "APPDATA", tempDir.resolve("Roaming").toString()
                ),
                tempDir
        );

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(OperatingSystem.WINDOWS)
        );

        assertEquals(
                "LOCALAPPDATA is unavailable",
                exception.getMessage()
        );
    }

    @Test
    void should_use_local_app_data_for_windows(@TempDir Path tempDir) {
        //given
        Path localAppData = tempDir.resolve("LocalAppData");

        LauncherUserPathsResolver resolver = new LauncherUserPathsResolver(
                Map.of(
                        "LOCALAPPDATA", localAppData.toString(),
                        "APPDATA", tempDir.resolve("Roaming").toString()
                ),
                tempDir
        );

        //when
        LauncherUserPaths paths = resolver.resolve(OperatingSystem.WINDOWS);

        //then
        Path applicationDirectory =
                localAppData.resolve("keystone");

        assertEquals(
                applicationDirectory.resolve("keystone.properties"),
                paths.configurationFile()
        );

        assertEquals(
                applicationDirectory,
                paths.defaultLauncherDirectory()
        );
    }
}
