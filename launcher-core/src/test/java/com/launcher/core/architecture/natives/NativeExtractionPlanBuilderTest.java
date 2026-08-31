package com.launcher.core.architecture.natives;

import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.natives.NativeExtractionPlanBuilder;
import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.manifest.natives.NativeExtractionRules;
import com.launcher.model.manifest.natives.SelectedNativeArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeExtractionPlanBuilderTest {

    private LibraryEntry getLibraryEntry(String path) {
        return new LibraryEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test-url.com/" + path
        );
    }

    @Test
    void should_build_native_extraction_plan_from_runtime_library_selection() {
        //given
        DirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        NativeExtractionPlanBuilder builder = new NativeExtractionPlanBuilder(directoryProvider);

        //when
        NativeExtractionPlan result = builder.build(
                new RuntimeLibrarySelection(
                        List.of(getLibraryEntry("path.jar")),
                        List.of(new SelectedNativeArtifact(
                                getLibraryEntry("artifact.jar"),
                                new NativeExtractionRules(List.of())
                        ))
                )
        );

        //then
        assertFalse(result.artifacts().isEmpty());
        assertEquals(
                List.of(
                        new SelectedNativeArtifact(
                                getLibraryEntry("artifact.jar"),
                                new NativeExtractionRules(List.of())
                        )
                ),
                result.artifacts()
        );
        assertEquals(
                directoryProvider.directories().natives(),
                result.targetDirectory()
        );
    }

    @Test
    void should_build_empty_native_extraction_plan_when_selection_has_no_native_artifacts() {
        //given
        DirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        NativeExtractionPlanBuilder builder = new NativeExtractionPlanBuilder(directoryProvider);

        //when
        NativeExtractionPlan result = builder.build(
                new RuntimeLibrarySelection(
                        List.of(
                                new LibraryEntry(
                                        "path.jar",
                                        "sha256",
                                        123L,
                                        "http://localhost/path.jar"
                                )
                        ),
                        List.of()
                )
        );

        //then
        assertTrue(result.artifacts().isEmpty());
    }

    @Test
    void should_reject_null_runtime_library_selection() {
        //given
        DirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        NativeExtractionPlanBuilder builder = new NativeExtractionPlanBuilder(directoryProvider);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(null)
        );

        assertTrue(exception.getMessage().contains("selection"));
    }

    @Test
    void should_reject_null_directory_provider() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new NativeExtractionPlanBuilder(null)
        );

        assertTrue(exception.getMessage().contains("directoryProvider"));
    }

}
