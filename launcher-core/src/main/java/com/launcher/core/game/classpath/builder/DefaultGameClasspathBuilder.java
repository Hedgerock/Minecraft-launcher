package com.launcher.core.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class DefaultGameClasspathBuilder implements GameClasspathBuilder {

    public GameClasspath build(Manifest manifest, Path gameDirectory) {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(gameDirectory, "gameDirectory");

        List<String> entries = manifest.libraries().isEmpty()
                ? manifest.launchInfo().classpath()
                : manifest.libraries().stream()
                    .map(LibraryEntry::path)
                    .toList();

        return new GameClasspath(
                entries.stream()
                        .map(gameDirectory::resolve)
                        .toList()
        );
    }

}
