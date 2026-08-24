package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ManifestResources {

    private ManifestResources() {

    }

    public static List<ResourceEntry> from(Manifest manifest) {
        Objects.requireNonNull(manifest, "manifest");

        return Stream.concat(
                manifest.files().stream().map(ManifestResources::fromFile),
                manifest.libraries().stream().map(ManifestResources::fromLibrary)
        ).toList();
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

}
