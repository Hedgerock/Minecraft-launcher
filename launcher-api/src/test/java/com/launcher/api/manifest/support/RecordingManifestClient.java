package com.launcher.api.manifest.support;

import com.launcher.api.manifest.client.ManifestClient;

public final class RecordingManifestClient implements ManifestClient {
    private final byte[] bytes;

    public RecordingManifestClient(byte[] bytes) {
        this.bytes = bytes;
    }

    public RecordingManifestClient() {
        this.bytes = new byte[64];
    }

    @Override
    public String download() {
        return ManifestJsonProvider.getMinimumValidManifestJson();
    }

    @Override
    public byte[] downloadBytes() {
        return bytes;
    }
}
