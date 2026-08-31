package com.launcher.model.manifest.natives;

import com.launcher.model.manifest.LibraryEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectedNativeArtifactTest {
    private static final LibraryEntry DEFAULT_LIBRARY_ENTRY_ARTIFACT = new LibraryEntry(
            "path.jar",
            "sha256",
            123L,
            "http://localhost/path.jar"
    );

    private static final NativeExtractionRules DEFAULT_NATIVE_EXTRACTION_RULES = new NativeExtractionRules(
            List.of()
    );

    @Test
    void should_reject_null_artifact() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SelectedNativeArtifact(null, DEFAULT_NATIVE_EXTRACTION_RULES)
        );

        assertTrue(exception.getMessage().contains("artifact"));
    }

    @Test
    void should_reject_null_extraction_rules() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SelectedNativeArtifact(DEFAULT_LIBRARY_ENTRY_ARTIFACT, null)
        );

        assertTrue(exception.getMessage().contains("extractionRules"));
    }

}
