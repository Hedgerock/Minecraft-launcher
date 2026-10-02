package com.launcher.api.manifest.support;

import com.launcher.api.manifest.client.ManifestClient;

public final class FailingManifestClient implements ManifestClient {
    private final RuntimeException runtimeException;

    public FailingManifestClient(RuntimeException runtimeException) {
        this.runtimeException = runtimeException;
    }

    @Override
    public String download() {
        throw runtimeException;
    }

    @Override
    public byte[] downloadBytes() {
        throw runtimeException;
    }
}
