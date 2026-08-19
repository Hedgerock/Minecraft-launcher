package com.launcher.core.architecture.support.recording;

import com.launcher.core.manifest.ManifestService;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;

import java.util.List;

public final class RecordingManifestService implements ManifestService {

    @Override
    public Manifest loadManifest() {
        return new Manifest(
                "1.12.2",
                loaderInfo(),
                files(),
                launchInfo()
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
                        "second-jvm-argument"
                ),
                List.of(
                        "first-game-argument",
                        "second-game-argument"
                ),
                List.of(
                        "test-value.jar",
                        "test-value2.jar"
                )
        );
    }

    private List<FileEntry> files() {
        return List.of(
                new FileEntry("test-path", "test-sha256", 123L, "test-url")
        );
    }
}
