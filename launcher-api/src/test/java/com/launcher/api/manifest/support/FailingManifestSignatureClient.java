package com.launcher.api.manifest.support;

import com.launcher.api.manifest.client.ManifestSignatureClient;

public final class FailingManifestSignatureClient implements ManifestSignatureClient {
    private final RuntimeException exception;

    public FailingManifestSignatureClient(RuntimeException exception) {
        this.exception = exception;
    }

    @Override
    public byte[] download() {
        throw exception;
    }
}
