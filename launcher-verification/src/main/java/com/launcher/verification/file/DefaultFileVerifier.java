package com.launcher.verification.file;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.storage.file.FileMetadataReader;
import com.launcher.storage.hash.HashService;

import java.nio.file.Path;

public class DefaultFileVerifier implements FileVerifier {
    private final FileMetadataReader metadataReader;
    private final HashService hashService;

    public DefaultFileVerifier(FileMetadataReader metadataReader, HashService hashService) {
        this.metadataReader = metadataReader;
        this.hashService = hashService;
    }

    @Override
    public ResourceVerificationResult verify(Path filePath, ResourceEntry resourceEntry) {

        if (!metadataReader.exists(filePath)) {
            return result(resourceEntry, VerificationStatus.MISSING);
        }

        if (metadataReader.size(filePath) != resourceEntry.size()) {
            return result(resourceEntry, VerificationStatus.OUTDATED);
        }

        String actualHash = hashService.sha256(filePath);

        if (!actualHash.equals(resourceEntry.sha256())) {
            return result(resourceEntry, VerificationStatus.CORRUPTED);
        }

        return result(resourceEntry, VerificationStatus.VALID);
    }

    private ResourceVerificationResult result(ResourceEntry resourceEntry, VerificationStatus status) {
        return new ResourceVerificationResult(resourceEntry, status);
    }
}
