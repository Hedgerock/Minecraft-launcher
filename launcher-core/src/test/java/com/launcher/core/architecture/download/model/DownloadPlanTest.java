package com.launcher.core.architecture.download.model;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadPlanTest {

    private ResourceEntry getResourceEntry(String path) {
        return new ResourceEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test-url.com/"+path
        );
    }

    @Test
    void should_reject_null_resource_entry() {
        //given
        List<ResourceEntry> files = new ArrayList<>();

        files.add(getResourceEntry("file1.jar"));
        files.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new DownloadPlan(files)
        );
    }

    @Test
    void should_create_immutable_resources() {
        //given
        List<ResourceEntry> files = new ArrayList<>();

        files.add(getResourceEntry("file1.jar"));
        files.add(getResourceEntry("file2.jar"));

        DownloadPlan downloadPlan = new DownloadPlan(files);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> downloadPlan.resources().add(getResourceEntry("file3.jar")
                )
        );

    }

    @Test
    void should_reject_null_resources() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DownloadPlan(null)
        );

        assertTrue(exception.getMessage().contains("resources"));
    }

}
