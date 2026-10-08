package com.launcher.publisher.manifest;

import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import com.launcher.publisher.manifest.support.ManifestSignerFixture;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ed25519ManifestSignerTest {
    private final Ed25519ManifestSigner signer = new Ed25519ManifestSigner();
    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_reject_signature_verified_with_different_public_key() throws GeneralSecurityException {
        //given
        KeyPair signingKeyPair = fixture.generateKeyPair();
        KeyPair differentKeyPair = fixture.generateKeyPair();

        byte[] manifestBytes = fixture.getManifestBytes();

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
        KeyPair keyPair = fixture.generateKeyPair();

        byte[] manifestBytes = fixture.getManifestBytes();

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
        KeyPair keyPair = fixture.generateKeyPair();

        byte[] manifestBytes = fixture.getManifestBytes();

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
}
