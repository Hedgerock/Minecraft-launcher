package com.launcher.publisher.key;

import com.launcher.publisher.support.ManifestSignerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalManifestPublicKeyExportServiceTest {
    private final LocalManifestPublicKeyExportService service =
            new LocalManifestPublicKeyExportService();

    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_not_export_public_key_with_different_algorithm(
            @TempDir Path tempDir
    ) throws GeneralSecurityException {
        //given
        KeyPair rsaPair = fixture.generateRsaKeyPair();
        Path outputFile = tempDir.resolve("managed-manifest-public-key.der");

        //when & then
        assertThrows(
                GeneralSecurityException.class,
                () -> service.export(rsaPair.getPublic(), outputFile)
        );

        assertFalse(Files.exists(outputFile));
    }

    @Test
    void should_not_overwrite_existing_public_key_file(
            @TempDir Path tempDir
    ) throws IOException, GeneralSecurityException {
        //given
        KeyPair keyPair = fixture.generateKeyPair();

        Path outputFile = tempDir.resolve(
                "managed-manifest-public-key.der"
        );

        byte[] existingBytes = {
                10, 20, 30, 40
        };

        Files.write(outputFile, existingBytes);

        //when
        assertThrows(
                IOException.class,
                () -> service.export(
                        keyPair.getPublic(),
                        outputFile
                )
        );

        //then
        assertArrayEquals(
                existingBytes,
                Files.readAllBytes(outputFile)
        );
    }

    @Test
    void should_export_public_key_as_subject_public_key_info(
            @TempDir Path tempDir
    ) throws IOException, GeneralSecurityException {
        //given
        KeyPair keyPair = fixture.generateKeyPair();

        Path outputFile = tempDir.resolve(
                "managed-manifest-public-key.der"
        );

        //when
        service.export(
                keyPair.getPublic(),
                outputFile
        );

        //then
        assertTrue(Files.isRegularFile(outputFile));

        byte[] exportedBytes = Files.readAllBytes(outputFile);

        PublicKey restoredKey = KeyFactory
                .getInstance("Ed25519")
                .generatePublic(
                        new X509EncodedKeySpec(exportedBytes)
                );

        assertArrayEquals(
                keyPair.getPublic().getEncoded(),
                restoredKey.getEncoded()
        );
    }
}
