package com.launcher.verification.file;

import com.launcher.core.storage.file.FileStorage;
import com.launcher.model.manifest.FileEntry;
import com.launcher.storage.hash.HashService;
import com.launcher.verification.model.FileVerificationResult;
import com.launcher.verification.model.VerificationStatus;

import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFileVerifier implements FileVerifier {
    private final FileStorage fileStorage;
    private final HashService hashService;

    public DefaultFileVerifier(FileStorage fileStorage, HashService hashService) {
        this.fileStorage = fileStorage;
        this.hashService = hashService;
    }

    @Override
    public FileVerificationResult verify(Path filePath, FileEntry file) {

        if (!Files.exists(filePath)) {
            return result(file, VerificationStatus.MISSING);
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
