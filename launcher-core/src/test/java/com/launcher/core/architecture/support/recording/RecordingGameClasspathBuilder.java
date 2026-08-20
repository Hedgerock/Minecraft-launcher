package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;
import java.util.List;

public final class RecordingGameClasspathBuilder implements GameClasspathBuilder {

    private Manifest manifest;
    private Path gameDirectory;
    private GameClasspath gameClasspath;

    @Override
    public GameClasspath build(Manifest manifest, Path gameDirectory) {
        this.gameDirectory = gameDirectory;
        this.manifest = manifest;

        this.gameClasspath = new GameClasspath(
                List.of(Path.of("current-path"))
        );

        return gameClasspath;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }

    public GameClasspath getGameClasspath() {
        return gameClasspath;
    }
}
