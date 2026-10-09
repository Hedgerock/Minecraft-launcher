package com.launcher.publisher.key;

import com.launcher.publisher.support.Pkcs12KeyStoreFixture;
import com.launcher.publisher.support.ManifestSignerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.Signature;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pkcs12SigningKeyLoaderTest {
    private static final String ALIAS = "manifest-signing";
    private static final char[] PASSWORD = "test-password".toCharArray();
    private static final String SIGN_P12 = "signing.p12";

    private final Pkcs12SigningKeyLoader loader = new Pkcs12SigningKeyLoader();
    private final ManifestSignerFixture fixture = new ManifestSignerFixture();

    @Test
    void should_reject_private_key_with_mismatched_certificate(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        KeyPair signingPair = fixture.generateKeyPair();
        KeyPair certificatePair = fixture.generateKeyPair();
        Path keyStoreFile = tempDir.resolve(SIGN_P12);

        Pkcs12KeyStoreFixture.createKeyStore(
                keyStoreFile,
                ALIAS,
                PASSWORD,
                signingPair.getPrivate(),
                Pkcs12KeyStoreFixture.createCertificate(certificatePair)
        );

        //when & then
        assertThrows(
                KeyStoreException.class,
                () -> loader.load(keyStoreFile, ALIAS, PASSWORD)
        );
    }

    @Test
    void should_reject_private_key_with_different_algorithm(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        KeyPair keyPair = fixture.generateRsaKeyPair();
        Path keyStoreFile = tempDir.resolve(SIGN_P12);

        Pkcs12KeyStoreFixture.createKeyStore(keyStoreFile, ALIAS, PASSWORD, keyPair);

        //when & then
        assertThrows(
                InvalidKeyException.class,
                () -> loader.load(
                        keyStoreFile,
                        ALIAS,
                        PASSWORD
                )
        );
    }

    @Test
    void should_reject_incorrect_password(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        KeyPair keyPair = fixture.generateKeyPair();
        Path keystoreFile = tempDir.resolve(SIGN_P12);

        Pkcs12KeyStoreFixture.createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

        char[] incorrectPassword = "wrong-password".toCharArray();

        //when & then
        assertThrows(
                IOException.class,
                () -> loader.load(
                        keystoreFile,
                        ALIAS,
                        incorrectPassword
                )
        );
    }

    @Test
    void should_reject_missing_alias(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        KeyPair keyPair = fixture.generateKeyPair();
        Path keystoreFile = tempDir.resolve(SIGN_P12);

        Pkcs12KeyStoreFixture.createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

        //when & then
        assertThrows(
                KeyStoreException.class,
                () -> loader.load(
                        keystoreFile,
                        "unknown-alias",
                        PASSWORD
                )
        );
    }

    @Test
    void should_load_ed25519_private_key_from_pkcs12(
            @TempDir Path tempDir
    ) throws Exception {
        //given
        KeyPair keyPair = fixture.generateKeyPair();
        Path keystoreFile = tempDir.resolve(SIGN_P12);

        Pkcs12KeyStoreFixture.createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

        //when
        KeyPair loadedPair = loader.load(
                keystoreFile,
                ALIAS,
                PASSWORD
        );

        //then
        byte[] manifestBytes = "manifest".getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(loadedPair.getPrivate());
        signer.update(manifestBytes);

        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(manifestBytes);

        assertTrue(verifier.verify(signature));
        assertArrayEquals(
                keyPair.getPublic().getEncoded(),
                loadedPair.getPublic().getEncoded()
        );
    }

}
