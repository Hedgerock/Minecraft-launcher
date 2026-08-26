package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeLibraryMetadataTest {

    @Test
    void should_reject_null_library_artifact_metadata() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibraryMetadata(null)
        );

        assertTrue(exception.getMessage().contains("artifact"));
    }

}
