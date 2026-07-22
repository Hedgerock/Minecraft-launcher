package com.launcher.verification.model;

import java.util.List;

public record VerificationPlan(
        List<FileVerificationResult> files
) {
}
