package com.launcher.model.manifest;

import com.launcher.model.support.ManifestResourcesFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestLoadResultTest {
    private final Manifest manifest = new ManifestResourcesFixture().getManifest();
    private final RuntimeLibrarySelection runtimeLibrarySelection = new RuntimeLibrarySelection(
            List.of(),
            List.of()
    );

    @Test
    void should_reject_null_runtime_library_selection() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ManifestLoadResult(manifest, null)
        );

        assertTrue(exception.getMessage().contains("runtimeLibrarySelection"));
    }

    @Test
    void should_reject_null_manifest() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ManifestLoadResult(null, runtimeLibrarySelection)
        );

        assertTrue(exception.getMessage().contains("manifest"));
    }

}
