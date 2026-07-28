package com.launcher.verification.service;


import com.launcher.model.manifest.Manifest;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.file.FileStorage;
import com.launcher.storage.hash.HashService;
import com.launcher.verification.model.VerificationPlan;

public class DefaultVerificationService implements VerificationService {
    private final FileStorage fileStorage;
    private final DirectoryProvider directoryProvider;
    private final HashService hashService;

    public DefaultVerificationService(FileStorage fileStorage, DirectoryProvider directoryProvider, HashService hashService) {
        this.fileStorage = fileStorage;
        this.directoryProvider = directoryProvider;
        this.hashService = hashService;
    }

    @Override
    public VerificationPlan verify(Manifest manifest) {
        return null;
    }
}
