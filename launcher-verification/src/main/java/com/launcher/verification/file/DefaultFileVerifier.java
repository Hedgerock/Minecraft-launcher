package com.launcher.verification.file;

import com.launcher.model.manifest.FileEntry;
import com.launcher.storage.file.FileMetadataReader;
import com.launcher.storage.hash.HashService;
import com.launcher.verification.model.FileVerificationResult;
import com.launcher.verification.model.VerificationStatus;

import java.nio.file.Path;

public class DefaultFileVerifier implements FileVerifier {
    private final FileMetadataReader metadataReader;
    private final HashService hashService;

    public DefaultFileVerifier(FileMetadataReader metadataReader, HashService hashService) {
        this.metadataReader = metadataReader;
        this.hashService = hashService;
    }

    @Override
    public FileVerificationResult verify(Path filePath, FileEntry file) {

        if (!metadataReader.exists(filePath)) {
            return result(file, VerificationStatus.MISSING);
        }

        if (metadataReader.size(filePath) != file.size()) {
            return result(file, VerificationStatus.OUTDATED);
        }

        String actualHash = hashService.sha256(filePath);

        if (!actualHash.equals(file.sha256())) {
            return result(file, VerificationStatus.CORRUPTED);
        }

        return result(file, VerificationStatus.VALID);
    }

    private FileVerificationResult result(FileEntry fileEntry, VerificationStatus status) {
        return new FileVerificationResult(fileEntry, status);
    }
}
