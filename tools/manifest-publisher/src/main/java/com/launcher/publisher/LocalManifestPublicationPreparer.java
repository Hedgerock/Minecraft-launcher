package com.launcher.publisher;

import com.launcher.publisher.key.LocalManifestPublicKeyExportService;
import com.launcher.publisher.key.Pkcs12SigningKeyLoader;
import com.launcher.publisher.manifest.LocalManifestSignatureService;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Objects;

public final class LocalManifestPublicationPreparer {
    private final Pkcs12SigningKeyLoader keyLoader;
    private final LocalManifestSignatureService signatureService;
    private final LocalManifestPublicKeyExportService publicKeyExportService;

    public LocalManifestPublicationPreparer(
            Pkcs12SigningKeyLoader keyLoader,
            LocalManifestSignatureService signatureService,
            LocalManifestPublicKeyExportService publicKeyExportService
    ) {
        this.keyLoader = Objects.requireNonNull(
                keyLoader,
                "keyLoader"
        );
        this.signatureService = Objects.requireNonNull(
                signatureService,
                "signatureService"
        );
        this.publicKeyExportService = Objects.requireNonNull(
                publicKeyExportService,
                "publicKeyExportService"
        );
    }

    public void signManifest(
            Path keyStoreFile,
            String alias,
            char[] password,
            Path manifestFile,
            Path signatureFile
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(manifestFile, "manifestFile");
        Objects.requireNonNull(signatureFile, "signatureFile");

        KeyPair keyPair = keyLoader.load(
                keyStoreFile,
                alias,
                password
        );

        signatureService.createSignature(
                manifestFile,
                signatureFile,
                keyPair.getPrivate()
        );
    }

    public void exportPublicKey(
            Path keyStoreFile,
            String alias,
            char[] password,
            Path publicKeyFile
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(publicKeyFile, "publicKeyFile");

        KeyPair keyPair = keyLoader.load(
                keyStoreFile,
                alias,
                password
        );

        publicKeyExportService.export(
                keyPair.getPublic(),
                publicKeyFile
        );
    }
}
