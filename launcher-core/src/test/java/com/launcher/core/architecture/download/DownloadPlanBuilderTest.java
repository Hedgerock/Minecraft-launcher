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
        FileEntry validFileEntry = getTargetFileEntry();

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
        FileEntry fileEntry = getFileEntry();
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.CORRUPTED, fileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(fileEntry));
    }

    @Test
    void should_include_outdated_files_in_download_plan() {
        //given
        FileEntry fileEntry = getFileEntry();
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.OUTDATED, fileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(fileEntry));
    }

    @Test
    void should_include_missing_files_in_download_plan() {
        //given
        FileEntry fileEntry = getFileEntry();
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.MISSING, fileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.files(), List.of(fileEntry));
    }

    @Test
    void should_create_empty_download_plan_when_verification_plan_is_valid() {
        //given
        FileEntry fileEntry = getFileEntry();
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.VALID, fileEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertTrue(result.isEmpty());
        assertTrue(result.files().isEmpty());
    }
    private VerificationPlan getLoadedVerificationPlan() {
        FileEntry validFileEntry = getTargetFileEntry();
        FileEntry corruptedFileEntry = getFileEntry();
        FileEntry outdatedFileEntry = getFileEntry();
        FileEntry missingFileEntry = getFileEntry();

        return new VerificationPlan(
                List.of(
                        new FileVerificationResult(validFileEntry, VerificationStatus.VALID),
                        new FileVerificationResult(outdatedFileEntry, VerificationStatus.OUTDATED),
                        new FileVerificationResult(corruptedFileEntry, VerificationStatus.CORRUPTED),
                        new FileVerificationResult(missingFileEntry, VerificationStatus.MISSING)
                )
        );
    }

    private FileEntry getFileEntry() {
        return new FileEntry(
                "test-path",
                "sha256",
                123L,
                "https://test-url.com"
        );
    }

    private FileEntry getTargetFileEntry() {
        return new FileEntry(
                "target-path",
                "target-sha256",
                12345678L,
                "https://target-test-url.com"
        );
    }
}
