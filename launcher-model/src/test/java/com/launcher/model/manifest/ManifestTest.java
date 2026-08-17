package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestTest {

    private LoaderInfo getLoaderInfo() {
        return new LoaderInfo(
                "test-type",
                "0.16.10"
        );
    }

    private LaunchInfo getLaunchInfo() {
        return new LaunchInfo(
                "MainClass",
                List.of("jvm", "arg1", "arg2"),
                List.of("classpath", "arg1", "arg2")
        );
    }

    private FileEntry getFileEntry(String path) {
        return new FileEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test-url.com/"+path
        );
    }

    @Test
    void should_reject_null_file_entry() {
        //given
        List<FileEntry> files = new ArrayList<>();
        files.add(getFileEntry("first-path.jar"));
        files.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () ->
                new Manifest(
                        "1.12.2",
                        getLoaderInfo(),
                        files,
                        getLaunchInfo()
                )
        );
    }

    @Test
    void should_create_immutable_files() {
        //given
        List<FileEntry> files = new ArrayList<>();
        files.add(getFileEntry("first-path.jar"));
        files.add(getFileEntry("second-path.jar"));

        Manifest manifest = new Manifest(
                "1.7.10",
                getLoaderInfo(),
                files,
                getLaunchInfo()
        );

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> manifest.files().add(getFileEntry("third-path.jar"))
        );

    }

    @Test
    void should_reject_null_files() {

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Manifest(
                        "1.12.2",
                        getLoaderInfo(),
                        null,
                        getLaunchInfo()
                )
        );

        assertTrue(exception.getMessage().contains("files"));
    }

}
