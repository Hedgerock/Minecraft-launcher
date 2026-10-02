package com.launcher.api.manifest.support;

import com.launcher.api.manifest.signature.ManifestSignatureVerifier;

public final class RecordingManifestSignatureVerifier implements ManifestSignatureVerifier {
    private byte[] signatureBytes;
    private byte[] manifestBytes;

    private boolean wasCalled = false;

    @Override
    public void verify(byte[] manifestBytes, byte[] signatureBytes) {
        this.manifestBytes = manifestBytes;
        this.signatureBytes = signatureBytes;

        wasCalled = true;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public byte[] getManifestBytes() {
        return manifestBytes;
    }

    public boolean wasCalled() {
        return wasCalled;
    }
}
