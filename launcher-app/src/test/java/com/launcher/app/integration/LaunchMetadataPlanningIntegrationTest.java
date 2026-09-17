package com.launcher.app.integration;

import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.app.storage.directory.LocalDirectoryProvider;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.game.DefaultGameLaunchPlanBuilder;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.DefaultClasspathFormatter;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.ManifestJavaRuntimeSelector;
import com.launcher.core.runtime.compatibility.NoOpJavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.detection.NoOpJavaRuntimeVersionDetector;
import com.launcher.core.runtime.javaexecutable.checker.NoOpJavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.resolver.ManifestJavaExecutableReferenceResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaunchMetadataPlanningIntegrationTest {
    private DefaultGameLaunchPlanBuilder builder;
    private DirectoryProvider directoryProvider;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        directoryProvider = new LocalDirectoryProvider(
                new LauncherConfiguration(
                        URI.create("http://localhost"),
                        tempDir.resolve("launcher-dir")
                )
        );

        builder = getBuilder();
    }

    @Test
    void should_resolve_supported_placeholders_in_auth_args() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = getManifestJsonWithSupportedPlaceholdersInAuthArgs();

        ManifestLoadResult mapped = mapper.map(json);

        //when
        GameLaunchPlan result = builder.build(
                mapped.manifest(),
                mapped.runtimeLibrarySelection()
        );

        //then
        String expectedPath = directoryProvider.directories().game()
                .resolve("versions")
                .resolve("client.jar")
                .toString();

        assertEquals(
                List.of(
                        "java",
                        "-Xmx2G",
                        "-Djava.class.path=%s".formatted(expectedPath),
                        "net.minecraft.client.main.Main",
                        "--accessToken",
                        "${access_token}",
                        "--version",
                        "1.12.2"
                ),
                result.command()
        );
    }

    @Test
    void should_build_launch_command_without_auth_args() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = getManifestJsonWithoutAuthArgs();

        ManifestLoadResult mapped = mapper.map(json);

        //when
        GameLaunchPlan result = builder.build(
                mapped.manifest(),
                mapped.runtimeLibrarySelection()
        );

        //then
        String expectedPath = directoryProvider.directories().game()
                .resolve("versions")
                .resolve("client.jar")
                .toString();

        assertEquals(
                List.of(
                        "java",
                        "-Xmx2G",
                        "-Djava.class.path=%s".formatted(expectedPath),
                        "net.minecraft.client.main.Main",
                        "--version",
                        "1.12.2"
                ),
                result.command()
        );
    }

    @Test
    void should_build_launch_command() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = getManifestJson();

        ManifestLoadResult mapped = mapper.map(json);

        //when
        GameLaunchPlan result = builder.build(
                mapped.manifest(),
                mapped.runtimeLibrarySelection()
        );

        //then
        String expectedPath = directoryProvider.directories().game()
                .resolve("versions")
                .resolve("client.jar")
                .toString();

        assertEquals(
                List.of(
                        "java",
                        "-Xmx2G",
                        "-Djava.class.path=%s".formatted(expectedPath),
                        "net.minecraft.client.main.Main",
                        "--version",
                        "1.12.2",
                        "--accessToken",
                        "${access_token}"
                ),
                result.command()
        );
    }

    private DefaultGameLaunchPlanBuilder getBuilder() {
        return new DefaultGameLaunchPlanBuilder(
                directoryProvider,
                new DefaultGameLaunchCommandBuilder(
                        new DefaultLaunchArgumentResolver()
                ),
                new DefaultGameClasspathBuilder(
                        new SafeResourcePathResolver()
                ),
                new DefaultClasspathFormatter(),
                new ManifestJavaRuntimeSelector(
                        new ManifestJavaExecutableReferenceResolver()
                ),
                new NoOpJavaExecutableReadinessChecker(),
                reference -> reference,
                new NoOpJavaRuntimeVersionDetector(),
                new NoOpJavaRuntimeCompatibilityChecker()
        );
    }

    private JsonManifestMapper getMapper() {
        return new JsonManifestMapper(
                new DefaultRuntimeLibrarySelector(),
                () -> new RuntimeEnvironment(OperatingSystem.WINDOWS)
        );
    }

    private String getManifestJsonWithSupportedPlaceholdersInAuthArgs() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": [],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    },
                    "authArgs": ["--accessToken", "${access_token}", "--version", "${version_name}"]
                }
                """;

        return getManifestJson(launchInfoJson);
    }

    private String getManifestJsonWithoutAuthArgs() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": ["--version", "${version_name}"],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    }
                }
                """;

        return getManifestJson(launchInfoJson);
    }

    private String getManifestJson() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": ["--version", "${version_name}"],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    },
                    "authArgs": ["--accessToken", "${access_token}"]
                }
                """;

        return getManifestJson(launchInfoJson);
    }

    private String getManifestJson(String currentLaunchInfoJson) {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    %s,
                    "libraries": []
                }
                """.formatted(currentLaunchInfoJson);
    }
}
