package com.launcher.publisher.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Objects;

public final class LocalManifestSignatureService {
    private final Ed25519ManifestSigner signer;

    public LocalManifestSignatureService(Ed25519ManifestSigner signer) {
        this.signer = Objects.requireNonNull(signer, "signer");
    }

    public void createSignature(
            Path manifestFile,
            Path signatureFile,
            PrivateKey privateKey
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(manifestFile, "manifestFile");
        Objects.requireNonNull(signatureFile, "signatureFile");
        Objects.requireNonNull(privateKey, "privateKey");

        byte[] manifestBytes = Files.readAllBytes(manifestFile);
        byte[] signatureBytes = signer.sign(manifestBytes, privateKey);

        Files.write(
                signatureFile,
                signatureBytes,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
    }
}
