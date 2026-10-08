package com.launcher.publisher.manifest;

import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.publisher.manifest.support.ManifestSignerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalManifestSignatureServiceTest {
    private final LocalManifestSignatureService service =
            new LocalManifestSignatureService(
                    new Ed25519ManifestSigner()
            );

    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_not_create_signature_when_manifest_does_not_exist(
         @TempDir Path tempDir
    ) throws GeneralSecurityException {
        //given
        KeyPair keyPair = fixture.generateKeyPair();

        Path manifestFile = tempDir.resolve("missing.json");
        Path signatureFile = tempDir.resolve("manifest.sig");

        //when & then
        assertThrows(
                IOException.class,
                () -> service.createSignature(
                        manifestFile,
                        signatureFile,
                        keyPair.getPrivate()
                )
        );

        assertFalse(Files.exists(signatureFile));
    }

    @Test
    void should_not_overwrite_existing_signature(
            @TempDir Path tempDir
    ) throws IOException, GeneralSecurityException {
        //given
        KeyPair keyPair = fixture.generateKeyPair();

        Path manifestFile = tempDir.resolve("manifest.json");
        Path signatureFile = tempDir.resolve("manifest.sig");

        Files.writeString(
                manifestFile,
                ManifestSignerFixture.DEFAULT_MANIFEST_VALUE
        );

        byte[] existingSignature = {
            10, 20, 30, 40
        };

        Files.write(signatureFile, existingSignature);

        //when & then
        assertThrows(
                IOException.class,
                () -> service.createSignature(
                        manifestFile,
                        signatureFile,
                        keyPair.getPrivate()
                )
        );

        assertArrayEquals(
                existingSignature,
                Files.readAllBytes(signatureFile)
        );
    }

    @Test
    void should_create_valid_signature_without_modifying_manifest(
            @TempDir Path tempDir
    ) throws IOException, GeneralSecurityException {
        //given
        KeyPair keyPair = fixture.generateKeyPair();

        Path manifestFile = tempDir.resolve("manifest.json");
        Path signatureFile = tempDir.resolve("manifest.sig");

        byte[] manifestBytes = fixture.getManifestBytes();

        Files.write(manifestFile, manifestBytes);

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(
                        keyPair.getPublic()
                );

        //when
        service.createSignature(
                manifestFile,
                signatureFile,
                keyPair.getPrivate()
        );

        //then
        assertArrayEquals(
                manifestBytes,
                Files.readAllBytes(manifestFile)
        );

        assertTrue(Files.isRegularFile(signatureFile));

        byte[] signatureBytes = Files.readAllBytes(signatureFile);

        assertDoesNotThrow(
                () -> verifier.verify(manifestBytes, signatureBytes)
        );
    }

}
