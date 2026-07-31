package com.launcher.verification.model;

import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationPlanTest {

    @Test
    void should_be_valid_when_all_files_are_valid() {
        //given
        VerificationPlan plan = new VerificationPlan(
                List.of(
                    result("libraries/a.jar", VerificationStatus.VALID),
                        result("libraries/b.dat", VerificationStatus.VALID)
                )
        );

        //then
        assertTrue(plan.isValid());
        assertFalse(plan.hasMissingFiles());
        assertFalse(plan.hasOutdatedFiles());
        assertFalse(plan.hasCorruptedFiles());
    }

    @Test
    void should_have_missing_files_when_any_file_is_missed() {
        //given
        VerificationPlan plan = new VerificationPlan(
                List.of(
                        result("libraries/a.jar", VerificationStatus.VALID),
                        result("libraries/b.dat", VerificationStatus.MISSING)
                )
        );

        //then
        assertFalse(plan.isValid());
        assertTrue(plan.hasMissingFiles());
    }

    @Test
    void should_have_outdated_files_when_any_file_is_outdated() {
        //given
        VerificationPlan plan = new VerificationPlan(
                List.of(
                        result("libraries/a.jar", VerificationStatus.OUTDATED),
                        result("libraries/b.dat", VerificationStatus.VALID)
                )
        );

        //then
        assertFalse(plan.isValid());
        assertTrue(plan.hasOutdatedFiles());
    }

    @Test
    void should_have_corrupted_files_when_any_file_is_corrupted() {
        //given
        VerificationPlan plan = new VerificationPlan(
                List.of(
                        result("libraries/a.jar", VerificationStatus.VALID),
                        result("libraries/b.dat", VerificationStatus.CORRUPTED)
                )
        );

        //then
        assertFalse(plan.isValid());
        assertTrue(plan.hasCorruptedFiles());
    }

    private FileVerificationResult result(String path, VerificationStatus status) {
        return new FileVerificationResult(
                new FileEntry(
                        path,
                        "sha256",
                        123L,
                        "https://example.com/"+path
                ),
                status
        );
    }
}
