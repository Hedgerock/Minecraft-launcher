package com.launcher.verification.support;

import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.LaunchInfo;

import java.util.List;

public final class RecordingManifest {
    private final Manifest manifest;

    public RecordingManifest() {
        this.manifest = new Manifest(
                "1.12.2",
                new LoaderInfo(
                        "forge",
                        "0.16.10"
                ),
                List.of(
                        new FileEntry(
                                "file-path",
                                "sha256-file",
                                123L,
                                "http://localhost/files/current-file.jar"
                        )
                ),
                new LaunchInfo(
                        "TestMain",
                        List.of(
                                "first-jvm-argument",
                                "second-jvm-argument",
                                "-cp",
                                "${classpath}"
                        ),
                        List.of(
                                "first-game-argument",
                                "second-game-argument",
                                "-gameDir",
                                "${game_directory}"
                        ),
                        List.of(
                                "test-value.jar",
                                "test-value2.jar"
                        ),
                        "java-custom"
                ),
                List.of(
                        new LibraryEntry(
                                "libraries/org/example/example.jar",
                                "sha256-library",
                                123L,
                                "http://localhost/libraries/example.jar"
                        )
                )
        );
    }

    public Manifest getManifest() {
        return manifest;
    }

}
