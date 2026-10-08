package com.launcher.publisher.manifest;

import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ed25519ManifestSignerTest {
    private final Ed25519ManifestSigner signer = new Ed25519ManifestSigner();

    @Test
    void should_reject_signature_verified_with_different_public_key() throws GeneralSecurityException {
        //given
        KeyPair signingKeyPair = generateKeyPair();
        KeyPair differentKeyPair = generateKeyPair();

        byte[] manifestBytes = getManifestBytes();

        byte[] signature = signer.sign(
                manifestBytes,
                signingKeyPair.getPrivate()
        );

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(
                        differentKeyPair.getPublic()
                );

        //when & then
        assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(manifestBytes, signature)
        );
    }

    @Test
    void should_reject_manifest_modified_after_signing() throws GeneralSecurityException {
        //given
        KeyPair keyPair = generateKeyPair();

        byte[] manifestBytes = getManifestBytes();

        byte[] signature = signer.sign(
                manifestBytes,
                keyPair.getPrivate()
        );

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        //when
        manifestBytes[0] ^= 1;

        //then
        assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(manifestBytes, signature)
        );
    }

    @Test
    void should_create_signature_accepted_by_manifest_verifier() throws GeneralSecurityException {
        //given
        KeyPair keyPair = generateKeyPair();

        byte[] manifestBytes = getManifestBytes();

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(
                        keyPair.getPublic()
                );

        //when
        byte[] signature = signer.sign(
                manifestBytes,
                keyPair.getPrivate()
        );

        //then
        assertDoesNotThrow(
                () -> verifier.verify(manifestBytes, signature)
        );
    }

    private byte[] getManifestBytes() {
        return "Manifest value"
                .getBytes(StandardCharsets.UTF_8);
    }

    private KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("Ed25519");

        return keyPairGenerator.generateKeyPair();
    }
}
