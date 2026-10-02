package com.launcher.api.manifest.support;

import com.launcher.api.manifest.client.ManifestSignatureClient;

public final class RecordingManifestSignatureClient implements ManifestSignatureClient {
    private final byte[] bytes;
    private boolean wasCalled = false;

    public RecordingManifestSignatureClient(byte[] bytes) {
        this.bytes = bytes;
    }

    public RecordingManifestSignatureClient() {
        this.bytes = new byte[64];
    }

    @Override
    public byte[] download() {
        this.wasCalled = true;
        return bytes;
    }

    public boolean wasCalled() {
        return wasCalled;
    }
}
