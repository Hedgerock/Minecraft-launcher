package com.launcher.core.architecture.support.recording;

import com.launcher.core.manifest.ManifestService;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;

import java.util.List;

public final class RecordingManifestService implements ManifestService {

    @Override
    public ManifestLoadResult loadManifest() {
        RuntimeLibrarySelection runtimeLibrarySelection = new RuntimeLibrarySelection(
                libraries(),
                natives()
        );

        Manifest manifest = new Manifest(
                "${minecraft_version}",
                loaderInfo(),
                files(),
                launchInfo(),
                runtimeLibrarySelection.selectedArtifacts()
        );

        return new ManifestLoadResult(
                manifest,
                runtimeLibrarySelection
        );
    }

    public Manifest loadManifestWithEmptyLibraries() {
        return new Manifest(
                "${minecraft_version}",
                loaderInfo(),
                files(),
                launchInfo(),
                List.of()
        );
    }

    private List<LibraryEntry> libraries() {
        return List.of(
                new LibraryEntry(
                        "libraries/org/example/example.jar",
                        "sha256",
                        123L,
                        "https://example.com/example.jar"
                )
        );
    }

    private List<LibraryEntry> natives() {
        return List.of(
                new LibraryEntry(
                        "libraries/org/example/natives/example-natives.jar",
                        "sha256-natives",
                        123L,
                        "https://example.com/example-natives.jar"
                )
        );
    }

    private LoaderInfo loaderInfo() {
        return new LoaderInfo(
                "test-type",
                "1.7.10"
        );
    }

    private LaunchInfo launchInfo() {
        return new LaunchInfo(
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
        );
    }

    private List<FileEntry> files() {
        return List.of(
                new FileEntry("test-path", "test-sha256", 123L, "test-url")
        );
    }
}
