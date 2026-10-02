package com.launcher.api.manifest.signature;

import com.launcher.api.manifest.support.ManifestJsonProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ed25519ManifestSignatureVerifierTest {
    private static final String ALGORITHM = "Ed25519";

    @Test
    void should_verify_valid_signature() {
        //given
        KeyPair keyPair = generator().generateKeyPair();
        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        byte[] manifestBytes = getManifestBytes("1.7.10");
        byte[] signatureBytes = signatureBytes(keyPair, manifestBytes);

        //when & then
        assertDoesNotThrow(
                () -> verifier.verify(
                        manifestBytes,
                        signatureBytes
                )
        );
    }

    @Test
    void should_fail_when_manifest_signature_has_invalid_format() {
        //given
        KeyPair keyPair = generator().generateKeyPair();
        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        byte[] manifestBytes = getManifestBytes("1.7.10");

        //when & then
        assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(
                        manifestBytes,
                        new byte[]{(byte) 1, 2, 3}
                )
        );
    }

    @Test
    void should_fail_when_public_key_is_not_valid() {
        //given
        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(getInvalidPublicKey());

        //when & then
        ManifestSignatureVerificationException exception = assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(
                        getManifestBytes("1.7.10"),
                        new byte[64]
                )
        );

        assertEquals(
                "Manifest signature verification could not be initialized",
                exception.getMessage()
        );

        assertInstanceOf(
                InvalidKeyException.class,
                exception.getCause()
        );
    }

    @Test
    void should_fail_when_manifest_was_signed_with_different_key() {
        //given
        KeyPair trustedKeyPair =
                generator().generateKeyPair();

        KeyPair differentKeyPair =
                generator().generateKeyPair();

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(
                        trustedKeyPair.getPublic()
                );

        byte[] manifestBytes =
                getManifestBytes("1.7.10");

        byte[] signatureBytes =
                signatureBytes(
                        differentKeyPair,
                        manifestBytes
                );

        //when & then
        ManifestSignatureVerificationException exception = assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(manifestBytes, signatureBytes)
        );

        assertEquals(
                "Manifest signature is invalid",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_manifest_was_modified_after_signing() {
        //given
        KeyPair keyPair = generator().generateKeyPair();
        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        byte[] manifestBytes = getManifestBytes("1.7.10");
        byte[] anotherManifestBytes = getManifestBytes("1.8.0");

        byte[] signatureBytes = signatureBytes(keyPair, manifestBytes);

        //when & then
        ManifestSignatureVerificationException exception = assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(anotherManifestBytes, signatureBytes)
        );

        assertEquals(
                "Manifest signature is invalid",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_signature_was_modified() {
        //given
        KeyPair keyPair = generator().generateKeyPair();
        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        byte[] manifestBytes = getManifestBytes("1.7.10");

        byte[] signatureBytes = signatureBytes(keyPair, manifestBytes);

        signatureBytes[0] ^= 1;

        //when & then
        assertThrows(
                ManifestSignatureVerificationException.class,
                () -> verifier.verify(manifestBytes, signatureBytes)
        );
    }

    @Test
    void should_reject_null_signature_bytes() {
        //given
        KeyPair keyPair = generator().generateKeyPair();

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> verifier.verify(
                        getManifestBytes("1.12.2"),
                        null
                )
        );

        assertEquals(
                "signatureBytes",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_bytes() {
        //given
        KeyPair keyPair = generator().generateKeyPair();

        Ed25519ManifestSignatureVerifier verifier =
                new Ed25519ManifestSignatureVerifier(keyPair.getPublic());

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> verifier.verify(null, new byte[64])
        );

        assertEquals(
                "manifestBytes",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_public_key() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Ed25519ManifestSignatureVerifier(null)
        );

        assertEquals(
                "publicKey",
                exception.getMessage()
        );
    }

    private byte[] signatureBytes(KeyPair keyPair, byte[] manifestBytes) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(keyPair.getPrivate());
            signature.update(manifestBytes);

            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyPairGenerator generator() {
        try {
            return KeyPairGenerator.getInstance(ALGORITHM);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getManifestBytes(String version) {
        return ManifestJsonProvider.getMinimumValidManifestJson(version)
                .getBytes(StandardCharsets.UTF_8);
    }

    private PublicKey getInvalidPublicKey() {
        return new PublicKey() {
            @Override
            public String getAlgorithm() {
                return ALGORITHM;
            }

            @Override
            public String getFormat() {
                return "X.509";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        };
    }
}
