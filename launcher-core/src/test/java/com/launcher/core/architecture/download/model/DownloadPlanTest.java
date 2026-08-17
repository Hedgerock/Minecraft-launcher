package com.launcher.core.architecture.download.model;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadPlanTest {

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

        files.add(getFileEntry("file1.jar"));
        files.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new DownloadPlan(files)
        );
    }

    @Test
    void should_create_immutable_files() {
        //given
        List<FileEntry> files = new ArrayList<>();

        files.add(getFileEntry("file1.jar"));
        files.add(getFileEntry("file2.jar"));

        DownloadPlan downloadPlan = new DownloadPlan(files);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> downloadPlan.files().add(getFileEntry("file3.jar")
                )
        );

    }

    @Test
    void should_reject_null_files() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DownloadPlan(null)
        );

        assertTrue(exception.getMessage().contains("files"));
    }

}
