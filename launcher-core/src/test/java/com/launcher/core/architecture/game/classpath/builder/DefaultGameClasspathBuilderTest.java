package com.launcher.core.architecture.game.classpath.builder;

import com.launcher.core.architecture.support.model.TestDefaultGameClasspathBuilderResourcePathResolverRecord;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.architecture.support.recording.RecordingResourcePathResolver;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameClasspathBuilderTest {
    private RecordingResourcePathResolver resourcePathResolver;
    private DefaultGameClasspathBuilder builder;

    private ManifestLoadResult getManifestWithEmptyLibraries() {
        return new RecordingManifestService().loadManifestWithEmptyLibraries();
    }

    private ManifestLoadResult getManifestLoadResult() {
        return new RecordingManifestService().loadManifest();
    }

    private ManifestLoadResult getManifestLoadResultWithNativeArtifactsAndWithoutLibraries() {
        return new RecordingManifestService().loadManifestWithNativeArtifactsAndWithoutLibraries();
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
    void should_use_launch_info_classpath_when_selected_libraries_are_empty_even_if_manifest_libraries_contains_native_artifacts() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResultWithNativeArtifactsAndWithoutLibraries();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();
        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, runtimeLibrarySelection.libraries(), gameDirectory);

        //then
        assertEquals(
                List.of(
                        gameDirectory.resolve("test-value.jar"),
                        gameDirectory.resolve("test-value2.jar")
                ),
                classpath.entries()
        );
    }

    @Test
    void should_use_resource_path_resolver_for_fallback_case() {
        //given
        ManifestLoadResult loadResult = getManifestWithEmptyLibraries();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        Path gameDirectory = Path.of("game-directory");

        //when
        builder.build(manifest, runtimeLibrarySelection.libraries(), gameDirectory);

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
    void should_reject_null_library_in_libraries() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();
        List<LibraryEntry> libraries = new ArrayList<>(runtimeLibrarySelection.libraries());

        libraries.add(null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(manifest, libraries, Path.of("game-directory"))
        );

        assertTrue(exception.getMessage().contains("library"));
    }

    @Test
    void should_reject_null_libraries() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();

        Path gameDirectory = Path.of("game-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(manifest, null, gameDirectory)
        );

        assertTrue(exception.getMessage().contains("libraries"));
    }

    @Test
    void should_use_resolved_paths_as_game_classpath_entries() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(
                manifest,
                runtimeLibrarySelection.libraries(),
                gameDirectory
        );

        //then
        assertEquals(
                resourcePathResolver.getResolvedPaths(),
                classpath.entries()
        );
    }

    @Test
    void should_pass_game_directory_and_resource_path_to_resource_path_resolver() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        Path gameDirectory = Path.of("game-directory");

        //when
        builder.build(
                manifest,
                runtimeLibrarySelection.libraries(),
                gameDirectory
        );

        //then
        List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> expectedRecords = getRecords(
                gameDirectory,
                "libraries/org/example/example.jar"
        );

        assertEquals(
                expectedRecords,
                resourcePathResolver.getRecords()
        );
    }

    @Test
    void should_build_game_classpath_from_launch_info_classpath_when_libraries_are_empty() {
        //given
        ManifestLoadResult manifestLoadResult = getManifestWithEmptyLibraries();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, runtimeLibrarySelection.libraries(), gameDirectory);

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
                () -> builder.build(null, List.of(), gameDirectory)
        );

        assertTrue(exception.getMessage().contains("manifest"));
    }

    @Test
    void should_reject_null_game_directory() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(manifest, runtimeLibrarySelection.libraries(), null)
        );

        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

    @Test
    void should_build_game_classpath_from_selected_runtime_libraries() {
        //given
        ManifestLoadResult loadResult = getManifestLoadResult();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(manifest, runtimeLibrarySelection.libraries(), gameDirectory);

        //then
        assertEquals(
                List.of(
                        gameDirectory.resolve("libraries/org/example/example.jar")
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
