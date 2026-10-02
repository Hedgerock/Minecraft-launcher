package com.launcher.api.manifest.signature;

public interface ManifestSignatureVerifier {

    void verify(byte[] manifestBytes, byte[] signatureBytes);
}
