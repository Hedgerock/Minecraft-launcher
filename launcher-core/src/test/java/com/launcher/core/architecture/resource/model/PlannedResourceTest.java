package com.launcher.core.architecture.resource.model;

import com.launcher.core.resource.model.PlannedResource;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlannedResourceTest {
    private static final Path DEFAULT_PATH = Path.of("test");
    private static final ResourceEntry DEFAULT_RESOURCE_ENTRY = new ResourceEntry(
            "test.jar",
            "test-sha256",
            123L,
            "https://test-url.com/test.jar"
    );

    @Test
    void should_create_planned_resource() {
        //given & when
        PlannedResource result = new PlannedResource(DEFAULT_RESOURCE_ENTRY, DEFAULT_PATH);

        //then
        assertEquals(DEFAULT_RESOURCE_ENTRY, result.resource());
        assertEquals(DEFAULT_PATH, result.targetPath());
    }

    @Test
    void should_reject_null_target_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PlannedResource(DEFAULT_RESOURCE_ENTRY, null)
        );

        assertEquals(
                "targetPath",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_resource() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PlannedResource(null, DEFAULT_PATH)
        );

        assertEquals(
                "resource",
                exception.getMessage()
        );
    }
}
