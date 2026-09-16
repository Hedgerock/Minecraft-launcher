package com.launcher.model.manifest;

import com.launcher.model.manifest.assets.AssetEntry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ManifestResources {

    private ManifestResources() {

    }

    public static List<ResourceEntry> from(Manifest manifest) {
        Objects.requireNonNull(manifest, "manifest");

        return Stream.of(
                manifest.files().stream().map(ManifestResources::fromFile),
                manifest.libraries().stream().map(ManifestResources::fromLibrary),
                manifest.assetsIndex().assets().stream().map(ManifestResources::fromAsset)
        )
                .flatMap(stream -> stream)
                .toList();
    }

    private static ResourceEntry fromFile(FileEntry file) {
        return new ResourceEntry(
                file.path(),
                file.sha256(),
                file.size(),
                file.url()
        );
    }

    private static ResourceEntry fromLibrary(LibraryEntry library) {
        return new ResourceEntry(
                library.path(),
                library.sha256(),
                library.size(),
                library.url()
        );
    }

    private static ResourceEntry fromAsset(AssetEntry asset) {
        return new ResourceEntry(
                asset.path(),
                asset.sha256(),
                asset.size(),
                asset.url()
        );
    }

}
