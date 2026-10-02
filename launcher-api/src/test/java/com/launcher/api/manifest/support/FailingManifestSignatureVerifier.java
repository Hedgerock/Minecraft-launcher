package com.launcher.api.manifest.support;

import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import com.launcher.api.manifest.signature.ManifestSignatureVerifier;

public final class FailingManifestSignatureVerifier implements ManifestSignatureVerifier {
    private final ManifestSignatureVerificationException exception;

    public FailingManifestSignatureVerifier(ManifestSignatureVerificationException exception) {
        this.exception = exception;
    }

    @Override
    public void verify(byte[] manifestBytes, byte[] signatureBytes) {
        throw exception;
    }
}
