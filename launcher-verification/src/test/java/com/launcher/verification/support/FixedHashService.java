package com.launcher.verification.support;

import com.launcher.storage.hash.HashService;

import java.nio.file.Path;

public final class FixedHashService implements HashService {
    private final String sha256;

    public FixedHashService(String sha256) {
        this.sha256 = sha256;
    }

    @Override
    public String sha256(Path filePath) {
        return sha256;
    }
}
