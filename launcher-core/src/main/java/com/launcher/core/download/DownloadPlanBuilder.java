package com.launcher.core.download;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;

import java.util.List;

public final class DownloadPlanBuilder {

    public DownloadPlan build(VerificationPlan verificationPlan) {
        List<FileEntry> files = verificationPlan.files().stream()
                .filter(this::isNotValidFile)
                .map(FileVerificationResult::file)
                .toList();

        return new DownloadPlan(files);
    }

    private boolean isNotValidFile(FileVerificationResult file) {
        return file.status() != VerificationStatus.VALID;
    }
}
