package com.launcher.verification.model;

import com.launcher.model.manifest.FileEntry;

public record FileVerificationResult(
        FileEntry file,
        VerificationStatus status
) {
}
