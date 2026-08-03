package com.launcher.verification.service;

import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.verification.VerificationService;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.verification.file.FileVerifier;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;

import java.nio.file.Path;
import java.util.List;

public class DefaultVerificationService implements VerificationService {
    private final DirectoryProvider directoryProvider;
    private final FileVerifier fileVerifier;

    public DefaultVerificationService(DirectoryProvider directoryProvider, FileVerifier fileVerifier) {
        this.directoryProvider = directoryProvider;
        this.fileVerifier = fileVerifier;
    }

    @Override
    public VerificationPlan verify(Manifest manifest) {
        List<FileVerificationResult> results = manifest.files()
                .stream()
                .map(this::verifyFile)
                .toList();

        return new VerificationPlan(results);
    }

    private FileVerificationResult verifyFile(FileEntry file) {
        Path filePath = directoryProvider.directories().launcher().resolve(file.path());

        return fileVerifier.verify(filePath, file);
    }
}
