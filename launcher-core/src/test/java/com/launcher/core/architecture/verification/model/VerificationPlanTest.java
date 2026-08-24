package com.launcher.core.architecture.verification.model;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationPlanTest {

    private ResourceEntry getResourceEntry(String path) {
        return new ResourceEntry(
                path,
                "sha256-" + path,
                123L,
                "http://test-path/" + path
        );
    }
    private ResourceVerificationResult getResourceVerificationResult(
            ResourceEntry resourceEntry,
            VerificationStatus status
    ) {
        return new ResourceVerificationResult(
                resourceEntry,
                status
        );
    }

    @Test
    void should_reject_null_result() {
        //given
        List<ResourceVerificationResult> verificationResults = new ArrayList<>();

        verificationResults
                .add(null);
        verificationResults
                .add(
                        getResourceVerificationResult(
                                getResourceEntry("file1.jar"),
                                VerificationStatus.MISSING
                        )
                );

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new VerificationPlan(verificationResults)
        );
    }

    @Test
    void should_create_immutable_resources() {
        //given
        List<ResourceVerificationResult> verificationResults = new ArrayList<>();

        verificationResults
                .add(
                        getResourceVerificationResult(
                                getResourceEntry("file1.jar"),
                                VerificationStatus.VALID
                        )
                );
        verificationResults
                .add(
                        getResourceVerificationResult(
                                getResourceEntry("file1.jar"),
                                VerificationStatus.MISSING
                        )
                );

        VerificationPlan verificationPlan =
                new VerificationPlan(verificationResults);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> verificationPlan.resources().add(
                        getResourceVerificationResult(
                                getResourceEntry("file3.jar"),
                                VerificationStatus.CORRUPTED
                        )
                )
        );
    }

    @Test
    void should_reject_null_resources() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new VerificationPlan(null)
        );

        assertTrue(exception.getMessage().contains("resources"));
    }

}
