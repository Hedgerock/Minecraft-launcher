package com.launcher.core.architecture.download;

import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadPlanBuilderTest {
    private final DownloadPlanBuilder builder = new DownloadPlanBuilder();
    private VerificationPlan getVerificationPlan(
            VerificationStatus status,
            ResourceEntry resourceEntry
    ) {
        return new VerificationPlan(
                List.of(new ResourceVerificationResult(resourceEntry, status))
        );
    }

    @Test
    void should_not_include_valid_resources_in_download_plan() {
        //given
        VerificationPlan plan = getLoadedVerificationPlan();
        ResourceEntry validFileEntry = getResourceEntry("valid.jar");

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(3, result.resources().size());
        assertFalse(result.resources().contains(validFileEntry));
    }

    @Test
    void should_include_corrupted_resources_in_download_plan() {
        //given
        ResourceEntry corruptedResourceEntry = getResourceEntry("corrupted.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.CORRUPTED, corruptedResourceEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.resources(), List.of(corruptedResourceEntry));
    }

    @Test
    void should_include_outdated_resources_in_download_plan() {
        //given
        ResourceEntry outdatedResourceEntry = getResourceEntry("outdated.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.OUTDATED, outdatedResourceEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.resources(), List.of(outdatedResourceEntry));
    }

    @Test
    void should_include_missing_resources_in_download_plan() {
        //given
        ResourceEntry missingResourceEntry = getResourceEntry("missing.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.MISSING, missingResourceEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertFalse(result.isEmpty());
        assertEquals(result.resources(), List.of(missingResourceEntry));
    }

    @Test
    void should_create_empty_download_plan_when_verification_plan_is_valid() {
        //given
        ResourceEntry validResourceEntry = getResourceEntry("valid.jar");
        VerificationPlan plan =
                getVerificationPlan(VerificationStatus.VALID, validResourceEntry);

        //when
        DownloadPlan result = builder.build(plan);

        //then
        assertTrue(result.isEmpty());
        assertTrue(result.resources().isEmpty());
    }
    private VerificationPlan getLoadedVerificationPlan() {
        ResourceEntry validResourceEntry = getResourceEntry("valid.jar");
        ResourceEntry corruptedResourceEntry = getResourceEntry("corrupted.jar");
        ResourceEntry outdatedResourceEntry = getResourceEntry("outdated.jar");
        ResourceEntry missingResourceEntry = getResourceEntry("missing.jar");

        return new VerificationPlan(
                List.of(
                        new ResourceVerificationResult(validResourceEntry, VerificationStatus.VALID),
                        new ResourceVerificationResult(outdatedResourceEntry, VerificationStatus.OUTDATED),
                        new ResourceVerificationResult(corruptedResourceEntry, VerificationStatus.CORRUPTED),
                        new ResourceVerificationResult(missingResourceEntry, VerificationStatus.MISSING)
                )
        );
    }

    private ResourceEntry getResourceEntry(String path) {
        return new ResourceEntry(
                path,
                "sha256-" + path,
                123L,
                "https://test-url.com/"+path
        );
    }

}
