package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManifestTest {

    @Test
    void should_create_immutable_libraries() {
        //given
        List<LibraryEntry> libraries = new ArrayList<>();

        libraries.add(new LibraryEntry("libraries/example.jar"));

        Manifest manifest = new Manifest(
                "1.12.2",
                getLoaderInfo(),
                List.of(getFileEntry("test.jar")),
                getLaunchInfo(),
                libraries
        );

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> manifest.libraries().add(new LibraryEntry("libraries/example2.jar"))
        );
    }

    @Test
    void should_protect_libraries_from_external_mutations() {
        //given
        List<LibraryEntry> libraries = new ArrayList<>();

        libraries.add(new LibraryEntry("libraries/example.jar"));

        Manifest manifest = new Manifest(
                "1.12.2",
                getLoaderInfo(),
                List.of(getFileEntry("test.jar")),
                getLaunchInfo(),
                libraries
        );

        //when
        libraries.add(new LibraryEntry("libraries/example2.jar"));

        //then
        assertEquals(1, manifest.libraries().size());
        assertEquals("libraries/example.jar", manifest.libraries().getFirst().path());

    }

    @Test
    void should_reject_null_libraries_entry() {
        //given
        List<LibraryEntry> libraries = new ArrayList<>();
        libraries.add(null);

        //when
        assertThrows(NullPointerException.class, () -> new Manifest(
                "1.12.2",
                getLoaderInfo(),
                List.of(getFileEntry("test.jar")),
                getLaunchInfo(),
                libraries
        ));

    }

    @Test
    void should_reject_null_libraries() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Manifest(
                        "1.12.2",
                        getLoaderInfo(),
                        List.of(getFileEntry("test.jar")),
                        getLaunchInfo(),
                        null
                )
        );

        assertTrue(exception.getMessage().contains("libraries"));
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
                getLaunchInfo(),
                getLibraries()
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
                        getLaunchInfo(),
                        getLibraries()
                )
        );

        assertTrue(exception.getMessage().contains("files"));
    }

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
                List.of("--username", "Player", "--userRole"),
                List.of("libraries/example.jar", "client.jar"),
                "java"
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

    private List<LibraryEntry> getLibraries() {
        return List.of(
                new LibraryEntry("libraries/example.jar")
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
                                getLaunchInfo(),
                                getLibraries()
                        )
        );
    }

}
