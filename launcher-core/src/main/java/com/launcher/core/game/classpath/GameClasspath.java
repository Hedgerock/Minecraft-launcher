package com.launcher.core.game.classpath;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record GameClasspath(
        List<Path> entries
) {

    public GameClasspath {
        Objects.requireNonNull(entries, "entries");

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }

        entries = List.copyOf(entries);
    }

}
