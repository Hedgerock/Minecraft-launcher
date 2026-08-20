package com.launcher.core.architecture.game.classpath.builder;

import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameClasspathBuilderTest {
    private final DefaultGameClasspathBuilder builder = new DefaultGameClasspathBuilder();

    private Manifest getManifestWithEmptyLibraries() {
        return new RecordingManifestService().loadManifestWithEmptyLibraries();
    }

    private Manifest getManifest() {
        return new RecordingManifestService().loadManifest();
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
                        gameDirectory.resolve("libraries/org/example/example.jar")
                ),
                classpath.entries()
        );
    }

}
