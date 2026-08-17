package com.launcher.core.verification.model;

import com.launcher.model.manifest.FileEntry;

import java.util.Objects;

public record FileVerificationResult(
        FileEntry file,
        VerificationStatus status
) {

    public FileVerificationResult {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(status, "status");
    }
}
