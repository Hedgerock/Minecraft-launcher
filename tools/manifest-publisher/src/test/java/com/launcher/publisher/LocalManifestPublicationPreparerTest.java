package com.launcher.publisher;

import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.publisher.key.LocalManifestPublicKeyExportService;
import com.launcher.publisher.key.Pkcs12SigningKeyLoader;
import com.launcher.publisher.key.support.Pkcs12KeyStoreFixture;
import com.launcher.publisher.manifest.Ed25519ManifestSigner;
import com.launcher.publisher.manifest.LocalManifestSignatureService;
import com.launcher.publisher.manifest.support.ManifestSignerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalManifestPublicationPreparerTest {
    private static final String ALIAS = "manifest-signing";
    private static final char[] PASSWORD = "test-password".toCharArray();

    private final LocalManifestPublicationPreparer preparer =
            new LocalManifestPublicationPreparer(
                    new Pkcs12SigningKeyLoader(),
                    new LocalManifestSignatureService(
                            new Ed25519ManifestSigner()
                    ),
                    new LocalManifestPublicKeyExportService()
            );

    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_not_export_public_key_when_password_is_invalid(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        Path keyStoreFile = tempDir.resolve("signing.p12");
        Path publicKeyFile = tempDir.resolve("public-key.der");

        Pkcs12KeyStoreFixture.create(
                keyStoreFile,
                ALIAS,
                PASSWORD
        );

        char[] invalidPassword = "wrong-password".toCharArray();

        //when & then
        assertThrows(
                IOException.class,
                () -> preparer.exportPublicKey(
                        keyStoreFile,
                        ALIAS,
                        invalidPassword,
                        publicKeyFile
                )
        );

        assertFalse(Files.exists(publicKeyFile));
    }

    @Test
    void should_not_create_signature_when_password_is_invalid(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        Path keyStoreFile = tempDir.resolve("signing.p12");
        Path manifestFile = tempDir.resolve("manifest.json");
        Path signatureFile = tempDir.resolve("manifest.sig");

        Pkcs12KeyStoreFixture.create(
                keyStoreFile,
                ALIAS,
                PASSWORD
        );

        Files.writeString(
                manifestFile,
                ManifestSignerFixture.DEFAULT_MANIFEST_VALUE
        );

        char[] invalidPassword = "wrong-password".toCharArray();

        //when & then
        assertThrows(
                IOException.class,
                () -> preparer.signManifest(
                        keyStoreFile,
                        ALIAS,
                        invalidPassword,
                        manifestFile,
                        signatureFile
                )
        );

        assertFalse(Files.exists(signatureFile));
    }

    @Test
    void should_sign_manifest_and_export_matching_public_key(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        Path keystoreFile = tempDir.resolve("signing.p12");
        Path manifestFile = tempDir.resolve("manifest.json");
        Path signatureFile = tempDir.resolve("manifest.sig");
        Path publicKeyFile = tempDir.resolve("public-key.der");

        Pkcs12KeyStoreFixture.create(
                keystoreFile,
                ALIAS,
                PASSWORD
        );

        byte[] manifestBytes = fixture.getManifestBytes();

        Files.write(manifestFile, manifestBytes);

        //when
        preparer.signManifest(
                keystoreFile,
                ALIAS,
                PASSWORD,
                manifestFile,
                signatureFile
        );

        preparer.exportPublicKey(
                keystoreFile,
                ALIAS,
                PASSWORD,
                publicKeyFile
        );

        //then
        assertArrayEquals(
                manifestBytes,
                Files.readAllBytes(manifestFile)
        );

        byte[] signatureBytes = Files.readAllBytes(signatureFile);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile);

        PublicKey exportedPublicKey = KeyFactory
                .getInstance("Ed25519")
                .generatePublic(
                        new X509EncodedKeySpec(publicKeyBytes)
                );

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(exportedPublicKey);

        assertDoesNotThrow(
                () -> verifier.verify(
                        manifestBytes,
                        signatureBytes
                )
        );
    }
}
