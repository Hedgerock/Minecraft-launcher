package com.launcher.core.verification.model;

import java.util.List;
import java.util.Objects;

public record VerificationPlan(
        List<FileVerificationResult> files
) {
    public VerificationPlan {
        Objects.requireNonNull(files, "files");
        files = List.copyOf(files);
    }

   public boolean isValid() {
       return files.stream()
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
       return files.stream()
               .anyMatch(file -> file.status() == status);
    }

}
