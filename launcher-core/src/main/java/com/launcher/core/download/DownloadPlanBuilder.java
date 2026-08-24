package com.launcher.core.download;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;

import java.util.List;

public final class DownloadPlanBuilder {

    public DownloadPlan build(VerificationPlan verificationPlan) {
        List<ResourceEntry> files = verificationPlan.resources().stream()
                .filter(this::isNotValidResource)
                .map(ResourceVerificationResult::resource)
                .toList();

        return new DownloadPlan(files);
    }

    private boolean isNotValidResource(ResourceVerificationResult resource) {
        return resource.status() != VerificationStatus.VALID;
    }
}
