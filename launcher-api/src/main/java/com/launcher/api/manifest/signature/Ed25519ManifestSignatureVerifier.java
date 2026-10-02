package com.launcher.api.manifest.signature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Objects;

public final class Ed25519ManifestSignatureVerifier implements ManifestSignatureVerifier {
    private static final String ALGORITHM = "Ed25519";

    private final PublicKey publicKey;

    public Ed25519ManifestSignatureVerifier(PublicKey publicKey) {
        this.publicKey = Objects.requireNonNull(
                publicKey,
                "publicKey"
        );
    }

    @Override
    public void verify(byte[] manifestBytes, byte[] signatureBytes) {
        Objects.requireNonNull(manifestBytes, "manifestBytes");
        Objects.requireNonNull(signatureBytes, "signatureBytes");

        try {
            Signature verifier = Signature.getInstance(ALGORITHM);

            verifier.initVerify(publicKey);
            verifier.update(manifestBytes);

            if (!verifier.verify(signatureBytes)) {
                throw new ManifestSignatureVerificationException(
                        "Manifest signature is invalid"
                );
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ManifestSignatureVerificationException(
                    "Manifest signature verification could not be initialized",
                    e
            );
        } catch (SignatureException e) {
            throw new ManifestSignatureVerificationException(
                    "Manifest signature could not be verified",
                    e
            );
        }
    }
}
