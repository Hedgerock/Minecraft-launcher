package com.launcher.api.manifest.support.fixture;

import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.model.manifest.natives.NativeExtractionRules;
import com.launcher.model.manifest.natives.SelectedNativeArtifact;
import com.launcher.model.runtime.JavaVersionRequirement;

import java.util.List;

public final class ManifestLoadResultFixture {

    public ManifestLoadResult loadManifest() {
        return loadManifest(
                libraries(),
                natives(),
                loaderInfo(),
                files(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithNonHttpsScheme() {
        return loadManifest(
                failingLibrariesWithoutHttps(),
                natives(),
                loaderInfo(),
                files(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithFailingAssetsIndex() {
        return loadManifest(
                libraries(),
                natives(),
                loaderInfo(),
                files(),
                launchInfo(),
                failingAssetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithNonAbsoluteUri() {
        return loadManifest(
                libraries(),
                failingNatives(),
                loaderInfo(),
                files(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithoutHost() {
        return loadManifest(
                failingLibrariesWithMissingHost(),
                natives(),
                loaderInfo(),
                files(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithoutUriScheme() {
        return loadManifest(
                libraries(),
                natives(),
                loaderInfo(),
                failingFiles(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithRawUserInfo() {
        return loadManifest(
                libraries(),
                natives(),
                loaderInfo(),
                failingFilesWithRawUserInfo(),
                launchInfo(),
                assetsIndex()
        );
    }

    public ManifestLoadResult loadManifestWithFragment() {
        return loadManifest(
                libraries(),
                failingNativesWithFragment(),
                loaderInfo(),
                files(),
                launchInfo(),
                assetsIndex()
        );
    }

    private ManifestLoadResult loadManifest(
            List<LibraryEntry> libraryEntries,
            List<SelectedNativeArtifact> selectedNativeArtifacts,
            LoaderInfo loaderInfo,
            List<FileEntry> fileEntries,
            LaunchInfo launchInfo,
            AssetsIndex assetsIndex
    ) {
        RuntimeLibrarySelection runtimeLibrarySelection = new RuntimeLibrarySelection(
                libraryEntries,
                selectedNativeArtifacts
        );

        Manifest manifest = new Manifest(
                "${minecraft_version}",
                loaderInfo,
                fileEntries,
                launchInfo,
                runtimeLibrarySelection.selectedArtifacts(),
                assetsIndex
        );

        return new ManifestLoadResult(
                manifest,
                runtimeLibrarySelection
        );
    }

    private List<LibraryEntry> libraries() {
        return List.of(libraryEntry(
                "libraries/org/example/example.jar",
                "https://example.com/example.jar"
        ));
    }

    private List<LibraryEntry> failingLibrariesWithoutHttps() {
        return List.of(libraryEntry(
                "libraries/org/example/example.jar",
                "http://example.com/example.jar?password=1234"
        ));
    }

    private List<LibraryEntry> failingLibrariesWithMissingHost() {
        return List.of(libraryEntry(
                "libraries/org/example/example.jar",
                "https:/resource.jar"
        ));
    }

    private List<SelectedNativeArtifact> natives() {
        return List.of(
                new SelectedNativeArtifact(
                        libraryEntry(
                                "natives/org/example/example.jar",
                                "https://example.com/example-natives.jar"
                        ),
                        new NativeExtractionRules(List.of())
                )
        );
    }

    private List<SelectedNativeArtifact> failingNatives() {
        return List.of(
                new SelectedNativeArtifact(
                        libraryEntry(
                                "natives/org/example/example.jar",
                                "//:3000/example-natives.jar"
                        ),
                        new NativeExtractionRules(List.of())
                )
        );
    }

    private List<SelectedNativeArtifact> failingNativesWithFragment() {
        return List.of(
                new SelectedNativeArtifact(
                        libraryEntry(
                                "natives/org/example/example.jar",
                                "https://example.com/resource.jar#section"
                        ),
                        new NativeExtractionRules(List.of())
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
                "java-custom",
                new JavaVersionRequirement(17),
                List.of(
                        "--accessToken",
                        "${access_token}"
                )
        );
    }

    private List<FileEntry> files() {
        return List.of(
                fileEntry("https://example.org/file-entry-value?user=user&password=1234")
        );
    }

    private List<FileEntry> failingFiles() {
        return List.of(
                fileEntry("//example.org/file-entry-value?user=user&password=1234")
        );
    }

    private List<FileEntry> failingFilesWithRawUserInfo() {
        return List.of(
                fileEntry("https://user%20name:password@example.com/resource.jar")
        );
    }

    private LibraryEntry libraryEntry(
            String path,
            String url
    ) {
        return new LibraryEntry(
                path,
                "sha256",
                123L,
                url
        );
    }

    private FileEntry fileEntry(String url) {
        return new FileEntry(
                "test-path",
                "test-sha256",
                123L,
                url
        );
    }

    private AssetsIndex failingAssetsIndex() {
        return new AssetsIndex(
                List.of(assetEntry("https://example.com/resource file.jar")
                )
        );
    }

    private AssetsIndex assetsIndex() {
        return new AssetsIndex(
                List.of(assetEntry("https://example.org/asset-entry.jar")
                )
        );
    }

    private AssetEntry assetEntry(String url) {
        return new AssetEntry(
                "asset-entry/org/value.jar",
                "sha256",
                123L,
                url
        );
    }
}
