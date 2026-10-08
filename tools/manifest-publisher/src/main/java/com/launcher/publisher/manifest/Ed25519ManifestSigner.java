package com.launcher.publisher.manifest;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Objects;

public final class Ed25519ManifestSigner {
    private static final String ALGORITHM = "Ed25519";

    public byte[] sign(byte[] manifestBytes, PrivateKey privateKey)
            throws GeneralSecurityException {
        Objects.requireNonNull(manifestBytes, "manifestBytes");
        Objects.requireNonNull(privateKey, "privateKey");

        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(privateKey);
        signer.update(manifestBytes);

        return signer.sign();
    }
}
