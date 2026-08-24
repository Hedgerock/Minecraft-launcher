package com.launcher.model.support;


import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ResourceEntry;

import java.util.List;

public final class ManifestResourcesFixture {

    private static final FileEntry FILE_ENTRY = new FileEntry(
            "file-path",
            "file-sha256",
            12345L,
            "file-url"
    );

    private static final LibraryEntry LIBRARY_ENTRY = new LibraryEntry(
            "library-path",
            "library-sha256",
            54321L,
            "library-url"
    );

    private static final String DEFAULT_MINECRAFT_VERSION = "1.12.2";

    private static final LoaderInfo DEFAULT_LOADER_INFO = new LoaderInfo(
            "forge",
            "0.16.10"
    );

    private static final LaunchInfo DEFAULT_LAUNCH_INFO = new LaunchInfo(
            "MainClass",
            List.of("jvm", "arg1", "arg2"),
            List.of("--username", "Player", "--userRole"),
            List.of("classpath-path"),
            "java"
    );

    private static final Manifest MANIFEST = new Manifest(
            DEFAULT_MINECRAFT_VERSION,
            DEFAULT_LOADER_INFO,
            List.of(FILE_ENTRY),
            DEFAULT_LAUNCH_INFO,
            List.of(LIBRARY_ENTRY)
    );

    private static final Manifest MANIFEST_WITHOUT_RESOURCES = new Manifest(
            DEFAULT_MINECRAFT_VERSION,
            DEFAULT_LOADER_INFO,
            List.of(),
            DEFAULT_LAUNCH_INFO,
            List.of()
    );


    private ResourceEntry getResourceEntry(String path, String sha256, long size, String url) {
        return new ResourceEntry(path, sha256, size, url);
    }

    @SuppressWarnings("SameParameterValue")
    private ResourceEntry toResourceEntry(LibraryEntry libraryEntry) {
        return getResourceEntry(
                libraryEntry.path(),
                libraryEntry.sha256(),
                libraryEntry.size(),
                libraryEntry.url()
        );
    }

    @SuppressWarnings("SameParameterValue")
    private ResourceEntry toResourceEntry(FileEntry fileEntry) {
        return getResourceEntry(
                fileEntry.path(),
                fileEntry.sha256(),
                fileEntry.size(),
                fileEntry.url()
        );
    }

    public Manifest getManifest() {
        return MANIFEST;
    }

    public Manifest getManifestWithoutResources() {
        return MANIFEST_WITHOUT_RESOURCES;
    }

    public ResourceEntry getLibraryEntry() {
        return toResourceEntry(LIBRARY_ENTRY);
    }

    public ResourceEntry getFileEntry() {
        return toResourceEntry(FILE_ENTRY);
    }

    public ResourceEntry generateResourceEntry(String path, String sha256, long size, String url) {
        return getResourceEntry(path, sha256, size, url);
    }

}
