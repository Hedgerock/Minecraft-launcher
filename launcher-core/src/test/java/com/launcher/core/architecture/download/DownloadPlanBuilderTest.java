package com.launcher.core.architecture.download;

import com.launcher.core.download.DownloadPlan;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadPlanBuilderTest {
    private final DownloadPlanBuilder builder = new DownloadPlanBuilder();
    private VerificationPlan getVerificationPlan(
            VerificationStatus status,
            FileEntry fileEntry
    ) {
        return new VerificationPlan(
                List.of(new FileVerificationResult(fileEntry, status))
        );
    }

    @Test
    void should_not_include_valid_files_in_download_plan() {
        //given
        VerificationPlan plan = getLoadedVerificationPlan();
        FileEntry validFileEntry = getFileEntry("valid.jar");

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(3, result.files().size());
        assertFalse(result.files().contains(validFileEntry));
    }

    @Test
    void should_include_corrupted_files_in_download_plan() {
        //given
        FileEntry corruptedFileEntry = getFileEntry("corrupted.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.CORRUPTED, corruptedFileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(corruptedFileEntry));
    }

    @Test
    void should_include_outdated_files_in_download_plan() {
        //given
        FileEntry outdatedFileEntry = getFileEntry("outdated.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.OUTDATED, outdatedFileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(outdatedFileEntry));
    }

    @Test
    void should_include_missing_files_in_download_plan() {
        //given
        FileEntry missingFileEntry = getFileEntry("missing.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.MISSING, missingFileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(missingFileEntry));
    }

    @Test
    void should_create_empty_download_plan_when_verification_plan_is_valid() {
        //given
        FileEntry validFileEntry = getFileEntry("valid.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.VALID, validFileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertTrue(result.isEmpty());
        assertTrue(result.files().isEmpty());
    }
    private VerificationPlan getLoadedVerificationPlan() {
        FileEntry validFileEntry = getFileEntry("valid.jar");
        FileEntry corruptedFileEntry = getFileEntry("corrupted.jar");
        FileEntry outdatedFileEntry = getFileEntry("outdated.jar");
        FileEntry missingFileEntry = getFileEntry("missing.jar");

        return new VerificationPlan(
                List.of(
                        new FileVerificationResult(validFileEntry, VerificationStatus.VALID),
                        new FileVerificationResult(outdatedFileEntry, VerificationStatus.OUTDATED),
                        new FileVerificationResult(corruptedFileEntry, VerificationStatus.CORRUPTED),
                        new FileVerificationResult(missingFileEntry, VerificationStatus.MISSING)
                )
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

}
