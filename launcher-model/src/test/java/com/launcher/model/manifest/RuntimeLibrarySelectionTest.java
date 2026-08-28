package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeLibrarySelectionTest {
    private static final List<LibraryEntry> DEFAULT_LIBRARIES = List.of(
            getLibraryEntry("first-library-path"),
            getLibraryEntry("second-library-path")
    );

    private static final List<LibraryEntry> DEFAULT_NATIVE_ARTIFACTS = List.of(
            getLibraryEntry("first-artifact-path"),
            getLibraryEntry("second-artifact-path")
    );

    @Test
    void should_return_native_artifacts_when_libraries_are_empty() {
        //given & when
        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(List.of(), DEFAULT_NATIVE_ARTIFACTS);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("first-artifact-path"),
                        getLibraryEntry("second-artifact-path")
                ),
                selection.selectedArtifacts()
        );
    }

    @Test
    void should_return_libraries_when_native_artifacts_are_empty() {
        //given & when
        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, List.of());

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("first-library-path"),
                        getLibraryEntry("second-library-path")
                ),
                selection.selectedArtifacts()
        );
    }

    @Test
    void should_return_libraries_and_native_artifacts_when_both_exist() {
        //given & when
        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, DEFAULT_NATIVE_ARTIFACTS);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("first-library-path"),
                        getLibraryEntry("second-library-path"),
                        getLibraryEntry("first-artifact-path"),
                        getLibraryEntry("second-artifact-path")
                ),
                selection.selectedArtifacts()
        );

    }

    @Test
    void should_return_true_when_native_artifacts_exist() {
        //given & when
        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, DEFAULT_NATIVE_ARTIFACTS);

        //then
        assertTrue(selection.hasNativeArtifacts());
    }

    @Test
    void should_return_false_when_native_artifacts_are_empty() {
        //given & when
        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, List.of());

        //then
        assertFalse(selection.hasNativeArtifacts());
    }

    @Test
    void should_reject_null_entry_to_native_artifacts() {
        //given
        List<LibraryEntry> nativeArtifacts = new ArrayList<>();
        nativeArtifacts.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibrarySelection(DEFAULT_LIBRARIES, nativeArtifacts)
        );
    }

    @Test
    void should_reject_null_entry_to_libraries() {
        //given
        List<LibraryEntry> libraries = new ArrayList<>();
        libraries.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibrarySelection(libraries, DEFAULT_NATIVE_ARTIFACTS)
        );
    }

    @Test
    void should_reject_native_artifacts_mutation_from_accessor() {
        //given
        LibraryEntry candidate = getLibraryEntry("candidate-path2");
        List<LibraryEntry> nativeArtifacts = new ArrayList<>(DEFAULT_NATIVE_ARTIFACTS);

        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, nativeArtifacts);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> selection.nativeArtifacts().add(candidate)
        );
    }

    @Test
    void should_reject_libraries_mutation_from_accessor() {
        //given
        LibraryEntry candidate = getLibraryEntry("candidate-path");
        List<LibraryEntry> libraries = new ArrayList<>(DEFAULT_LIBRARIES);

        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(libraries, DEFAULT_NATIVE_ARTIFACTS);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> selection.libraries().add(candidate)
        );
    }

    @Test
    void should_create_immutable_native_artifacts() {
        //given
        LibraryEntry candidate = getLibraryEntry("candidate-path");
        List<LibraryEntry> nativeArtifacts = new ArrayList<>(DEFAULT_NATIVE_ARTIFACTS);

        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(DEFAULT_LIBRARIES, nativeArtifacts);

        //when
        nativeArtifacts.add(candidate);

        //then
        assertEquals(
                DEFAULT_NATIVE_ARTIFACTS,
                selection.nativeArtifacts()
        );
    }

    @Test
    void should_create_immutable_libraries() {
        //given
        LibraryEntry candidate = getLibraryEntry("candidate-path");
        List<LibraryEntry> libraries = new ArrayList<>(DEFAULT_LIBRARIES);

        RuntimeLibrarySelection selection = new RuntimeLibrarySelection(libraries, DEFAULT_NATIVE_ARTIFACTS);

        //when
        libraries.add(candidate);

        //then
        assertEquals(
                DEFAULT_LIBRARIES,
                selection.libraries()
        );

    }

    @Test
    void should_reject_null_native_artifacts() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibrarySelection(DEFAULT_LIBRARIES, null)
        );

        assertTrue(exception.getMessage().contains("nativeArtifacts"));
    }

    @Test
    void should_reject_null_libraries() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibrarySelection(null, DEFAULT_NATIVE_ARTIFACTS)
        );

        assertTrue(exception.getMessage().contains("libraries"));
    }

    private static LibraryEntry getLibraryEntry(String path) {
        return new LibraryEntry(
                path + ".jar",
                "sha256-" + path,
                123L,
                "http://localhost/" + path + ".jar"
        );
    }

}
