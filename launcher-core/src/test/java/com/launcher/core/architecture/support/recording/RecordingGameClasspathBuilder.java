package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;
import java.util.List;

public final class RecordingGameClasspathBuilder implements GameClasspathBuilder {

    private Manifest manifest;
    private Path gameDirectory;
    private GameClasspath gameClasspath;
    private List<LibraryEntry> libraryEntries;

    @Override
    public GameClasspath build(
            Manifest manifest,
            List<LibraryEntry> libraryEntries,
            Path gameDirectory
    ) {
        this.gameDirectory = gameDirectory;
        this.manifest = manifest;
        this.libraryEntries = libraryEntries;

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

    public List<LibraryEntry> getLibraryEntries() {
        return List.copyOf(libraryEntries);
    }
}
