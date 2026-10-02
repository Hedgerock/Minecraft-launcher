package com.launcher.api.manifest.signature;

public final class ManifestSignatureVerificationException extends RuntimeException {
    public ManifestSignatureVerificationException(String message) {
        super(message);
    }

    public ManifestSignatureVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
