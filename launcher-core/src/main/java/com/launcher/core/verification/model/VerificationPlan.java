package com.launcher.core.verification.model;

import java.util.List;
import java.util.Objects;

public record VerificationPlan(
        List<ResourceVerificationResult> resources
) {
    public VerificationPlan {
        Objects.requireNonNull(resources, "resources");
        resources = List.copyOf(resources);
    }

   public boolean isValid() {
       return resources.stream()
               .allMatch(file ->
                       file.status() == VerificationStatus.VALID
               );
   }

    public boolean hasMissingFiles() {
        return hasStatus(VerificationStatus.MISSING);
    }

    public boolean hasOutdatedFiles() {
        return hasStatus(VerificationStatus.OUTDATED);

    }

    public boolean hasCorruptedFiles() {
        return hasStatus(VerificationStatus.CORRUPTED);
    }

    private boolean hasStatus(VerificationStatus status) {
       return resources.stream()
               .anyMatch(resource -> resource.status() == status);
    }

}
