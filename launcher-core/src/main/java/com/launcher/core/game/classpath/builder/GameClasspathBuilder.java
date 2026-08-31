package com.launcher.core.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;
import java.util.List;

public interface GameClasspathBuilder {

    GameClasspath build(
            Manifest manifest,
            List<LibraryEntry> libraries,
            Path gameDirectory
    );

}
