package com.launcher.verification.support;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.verification.file.FileVerifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingFileVerifier implements FileVerifier {
    private final List<Path> paths = new ArrayList<>();
    private final List<ResourceEntry> resourceEntries = new ArrayList<>();
    private VerificationStatus verificationStatus = VerificationStatus.VALID;

    public List<Path> getPaths() {
        return List.copyOf(paths);
    }

    public List<ResourceEntry> getResourceEntries() {
        return List.copyOf(resourceEntries);
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    @Override
    public ResourceVerificationResult verify(Path filePath, ResourceEntry resourceEntry) {
       paths.add(filePath);
       resourceEntries.add(resourceEntry);

       return new ResourceVerificationResult(
               resourceEntry,
               verificationStatus
       );

    }
}
