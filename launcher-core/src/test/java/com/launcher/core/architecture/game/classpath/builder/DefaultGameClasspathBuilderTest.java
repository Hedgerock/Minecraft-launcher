package com.launcher.core.architecture.game.classpath.builder;

import com.launcher.core.architecture.support.model.TestDefaultGameClasspathBuilderResourcePathResolverRecord;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.architecture.support.recording.RecordingResourcePathResolver;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameClasspathBuilderTest {
    private RecordingResourcePathResolver resourcePathResolver;
    private DefaultGameClasspathBuilder builder;

    private Manifest getManifestWithEmptyLibraries() {
        return new RecordingManifestService().loadManifestWithEmptyLibraries();
    }

    private Manifest getManifest() {
        return new RecordingManifestService().loadManifest().manifest();
    }

    @BeforeEach
    void setUp() {
        resourcePathResolver = new RecordingResourcePathResolver();
        builder = new DefaultGameClasspathBuilder(resourcePathResolver);
    }

    @Test
    void should_reject_null_resource_path_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultGameClasspathBuilder(null)
        );

        assertTrue(exception.getMessage().contains("resourcePathResolver"));
    }

    @Test
    void should_use_resource_path_resolver_for_fallback_case() {
        //given
        Manifest manifest = getManifestWithEmptyLibraries();

        Path gameDirectory = Path.of("game-directory");

        //when
        builder.build(manifest, gameDirectory);

        //then
        List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> expectedRecords = getRecords(
                gameDirectory,
                "test-value.jar",
                "test-value2.jar"
        );

        assertEquals(
                expectedRecords,
                resourcePathResolver.getRecords()
        );
    }

    @Test
    void should_use_resolved_paths_as_game_classpath_entries() {
        //given
        Manifest manifest = getManifest();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, gameDirectory);

        //then
        assertEquals(
                resourcePathResolver.getResolvedPaths(),
                classpath.entries()
        );
    }

    @Test
    void should_pass_game_directory_and_resource_path_to_resource_path_resolver() {
        //given
        Manifest manifest = getManifest();

        Path gameDirectory = Path.of("game-directory");

        //when
        builder.build(manifest, gameDirectory);

        //then
        List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> expectedRecords = getRecords(
                gameDirectory,
                "libraries/org/example/example.jar",
                "libraries/org/example/natives/example-natives.jar"
        );

        assertEquals(
                expectedRecords,
                resourcePathResolver.getRecords()
        );
    }

    @Test
    void should_build_game_classpath_from_launch_info_classpath_when_libraries_are_empty() {
        //given
        Manifest manifest = getManifestWithEmptyLibraries();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, gameDirectory);

        //then
        assertTrue(manifest.libraries().isEmpty());

        assertEquals(
                List.of(
                        gameDirectory.resolve("test-value.jar"),
                        gameDirectory.resolve("test-value2.jar")
                ),
                classpath.entries()
        );
    }

    @Test
    void should_reject_null_manifest() {
        //given
        Path gameDirectory = Path.of("game-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(null, gameDirectory)
        );

        assertTrue(exception.getMessage().contains("manifest"));
    }

    @Test
    void should_reject_null_game_directory() {
        //given
        Manifest manifest = getManifest();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(manifest, null)
        );

        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

    @Test
    void should_build_game_classpath_from_manifest_libraries_classpath_entry() {
        //given
        Manifest manifest = getManifest();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, gameDirectory);

        //then
        assertEquals(
                List.of(
                        gameDirectory.resolve("libraries/org/example/example.jar"),
                        gameDirectory.resolve("libraries/org/example/natives/example-natives.jar")
                ),
                classpath.entries()
        );
    }

    private List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> getRecords(
            Path baseDirectory, String... resourcePaths
    ) {
        return Arrays.stream(resourcePaths)
                .map(resourcePath -> new TestDefaultGameClasspathBuilderResourcePathResolverRecord(
                        baseDirectory,
                        resourcePath
                ))
                .toList();
    }

}
