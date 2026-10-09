package com.launcher.publisher.support;

import com.launcher.publisher.LocalManifestPublicationPreparer;
import com.launcher.publisher.key.LocalManifestPublicKeyExportService;
import com.launcher.publisher.key.Pkcs12SigningKeyLoader;
import com.launcher.publisher.manifest.Ed25519ManifestSigner;
import com.launcher.publisher.manifest.LocalManifestSignatureService;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public final class ManifestSignerFixture {
    public static final String DEFAULT_MANIFEST_VALUE = "Manifest value";

    public static final LocalManifestPublicationPreparer PREPARER =
            new LocalManifestPublicationPreparer(
                    new Pkcs12SigningKeyLoader(),
                    new LocalManifestSignatureService(
                            new Ed25519ManifestSigner()
                    ),
                    new LocalManifestPublicKeyExportService()
            );

    public KeyPair generateKeyPair() throws GeneralSecurityException {
        return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    }

    public KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public byte[] getManifestBytes() {
        return DEFAULT_MANIFEST_VALUE.getBytes(StandardCharsets.UTF_8);
    }
}
