package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRuntimeLibrarySelectorTest {
    private final RuntimeLibrarySelector selector = new DefaultRuntimeLibrarySelector();

    @Test
    void should_reject_null_value_in_list_of_libraries() {
        //given
        List<RuntimeLibraryMetadata> libraries = new ArrayList<>();
        libraries.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> selector.select(libraries)
        );
    }

    @Test
    void should_reject_null_list_of_libraries() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> selector.select(null)
        );

        assertTrue(exception.getMessage().contains("libraries"));
    }

    @Test
    void should_return_empty_list_when_runtime_library_metadata_is_empty() {
        //given & when
        List<LibraryEntry> result = selector.select(List.of());

        //then
        assertEquals(
                List.of(),
                result
        );
    }

    @Test
    void should_select_runtime_libraries_as_library_entries() {
        //given
        RuntimeLibraryMetadata runtimeLibraryMetadata = getRuntimeLibraryMetadata("libraries/example.jar");

        //when
        List<LibraryEntry> result = selector.select(List.of(runtimeLibraryMetadata));

        //then
        assertEquals(
                List.of(getLibraryEntry("libraries/example.jar")),
                result
        );
    }

    @SuppressWarnings("SameParameterValue")
    private RuntimeLibraryMetadata getRuntimeLibraryMetadata(String path) {
        return new RuntimeLibraryMetadata(
                new LibraryArtifactMetadata(
                        path,
                        "sha256",
                        100L,
                        "https://example.com/" + path
                )
        );
    }

    @SuppressWarnings("SameParameterValue")
    private LibraryEntry getLibraryEntry(String path) {
        return new LibraryEntry(
                path,
                "sha256",
                100L,
                "https://example.com/" + path
        );
    }

}
