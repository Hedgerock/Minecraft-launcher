package com.launcher.verification.support;

import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.model.runtime.JavaVersionRequirement;

import java.util.List;

public final class RecordingManifest {
    private final Manifest manifest;
    private final Manifest manifestWithSingleResource;
    private final Manifest manifestWithTheSamePaths;
    private final Manifest manifestWithPotentialResolvedAsTheSameTarget;
    private final Manifest manifestWithIncompatibleResources;

    public RecordingManifest() {
        this.manifest = manifest();
        this.manifestWithSingleResource = manifestWithSingleResource();
        this.manifestWithTheSamePaths = manifestWithTheSamePaths();
        this.manifestWithPotentialResolvedAsTheSameTarget = manifestWithPotentialResolvedAsTheSameTarget();
        this.manifestWithIncompatibleResources = manifestWithIncompatibleResources();
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Manifest getManifestWithSingleResource() {
        return manifestWithSingleResource;
    }

    public Manifest getManifestWithTheSamePaths() {
        return manifestWithTheSamePaths;
    }

    public Manifest getManifestWithPotentialResolvedAsTheSameTarget() {
        return manifestWithPotentialResolvedAsTheSameTarget;
    }

    public Manifest getManifestWithIncompatibleResources() {
        return manifestWithIncompatibleResources;
    }

    private Manifest manifestWithIncompatibleResources() {
        FileEntry fileEntry = new FileEntry(
                "path/same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        LibraryEntry libraryEntry = new LibraryEntry(
                "libraries/org/example/example.jar",
                "sha256-library",
                123L,
                "http://localhost/libraries/example.jar"
        );

        AssetEntry assetEntry = new AssetEntry(
                "path/./same-path.jar",
                "same-sha256",
                312L,
                "http://localhost/files/same-path.jar"
        );

        return manifest(
                List.of(fileEntry),
                List.of(libraryEntry),
                List.of(assetEntry)
        );
    }

    private Manifest manifestWithPotentialResolvedAsTheSameTarget() {
        FileEntry fileEntry = new FileEntry(
                "path/same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        LibraryEntry libraryEntry = new LibraryEntry(
                "libraries/org/example/example.jar",
                "sha256-library",
                123L,
                "http://localhost/libraries/example.jar"
        );

        AssetEntry assetEntry = new AssetEntry(
                "path/./same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        return manifest(
                List.of(fileEntry),
                List.of(libraryEntry),
                List.of(assetEntry)
        );
    }

    private Manifest manifestWithTheSamePaths() {
        FileEntry fileEntry = new FileEntry(
                "same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        LibraryEntry libraryEntry = new LibraryEntry(
                "libraries/org/example/example.jar",
                "sha256-library",
                123L,
                "http://localhost/libraries/example.jar"
        );

        AssetEntry assetEntry = new AssetEntry(
                "same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        return manifest(
                List.of(fileEntry),
                List.of(libraryEntry),
                List.of(assetEntry)
        );
    }

    private Manifest manifestWithSingleResource() {
        FileEntry fileEntry = new FileEntry(
                "file-path",
                "sha256-file",
                123L,
                "http://localhost/files/current-file.jar"
        );

        return manifest(
                List.of(fileEntry),
                List.of(),
                List.of()
        );
    }

    private Manifest manifest() {
        FileEntry fileEntry = new FileEntry(
                "file-path",
                "sha256-file",
                123L,
                "http://localhost/files/current-file.jar"
        );

        LibraryEntry libraryEntry = new LibraryEntry(
                "libraries/org/example/example.jar",
                "sha256-library",
                123L,
                "http://localhost/libraries/example.jar"
        );

        return manifest(
                List.of(fileEntry),
                List.of(libraryEntry),
                List.of()
        );
    }

    private Manifest manifest(
            List<FileEntry> fileEntry,
            List<LibraryEntry> libraryEntry,
            List<AssetEntry> assetEntry
    ) {
        return new Manifest(
                "1.12.2",
                new LoaderInfo(
                        "forge",
                        "0.16.10"
                ),
                fileEntry,
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
                        "java-custom",
                        new JavaVersionRequirement(17)
                ),
                libraryEntry,
                new AssetsIndex(
                        assetEntry
                )
        );
    }
}
