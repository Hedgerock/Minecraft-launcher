package com.launcher.verification.model;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationPlanTest {

    @Test
    void should_be_valid_when_all_resources_are_valid() {
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
    void should_have_missing_resources_when_any_file_is_missed() {
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
    void should_have_outdated_resources_when_any_file_is_outdated() {
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
    void should_have_corrupted_resources_when_any_file_is_corrupted() {
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

    private ResourceVerificationResult result(String path, VerificationStatus status) {
        return new ResourceVerificationResult(
                new ResourceEntry(
                        path,
                        "sha256",
                        123L,
                        "https://example.com/"+path
                ),
                status
        );
    }
}
