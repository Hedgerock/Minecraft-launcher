package com.launcher.core.verification.model;

import com.launcher.model.manifest.ResourceEntry;

import java.util.Objects;

public record ResourceVerificationResult(
        ResourceEntry resource,
        VerificationStatus status
) {

    public ResourceVerificationResult {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(status, "status");
    }
}
