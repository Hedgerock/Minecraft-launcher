package com.launcher.publisher.key;

import com.launcher.publisher.manifest.support.ManifestSignerFixture;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

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

        createKeyStore(
                keyStoreFile,
                ALIAS,
                PASSWORD,
                signingPair.getPrivate(),
                createCertificate(certificatePair)
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

        createKeyStore(keyStoreFile, ALIAS, PASSWORD, keyPair);

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

        createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

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

        createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

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

        createKeyStore(keystoreFile, ALIAS, PASSWORD, keyPair);

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

    private void createKeyStore(
            Path keyStoreFile,
            String alias,
            char[] password,
            KeyPair keyPair
    ) throws Exception {
        X509Certificate certificate = createCertificate(keyPair);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, password);

        keyStore.setKeyEntry(
                alias,
                keyPair.getPrivate(),
                password,
                new Certificate[]{certificate}
        );

        try (OutputStream output = Files.newOutputStream(keyStoreFile)) {
            keyStore.store(output, password);
        }
    }

    private void createKeyStore(
            Path keyStoreFile,
            String alias,
            char[] password,
            PrivateKey privateKey,
            X509Certificate certificate
    ) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, password);

        keyStore.setKeyEntry(
                alias,
                privateKey,
                password,
                new Certificate[]{certificate}
        );

        try (OutputStream output = Files.newOutputStream(keyStoreFile)) {
            keyStore.store(output, password);
        }
    }

    private X509Certificate createCertificate(KeyPair keyPair) throws Exception {
        String signatureAlgorithm = switch (keyPair.getPrivate().getAlgorithm()) {
            case "EdDSA", "Ed25519" -> "Ed25519";
            case "RSA" -> "SHA256withRSA";
            default -> throw new IllegalArgumentException(
                    "Unsupported test key algorithm"
            );
        };

        Instant now = Instant.now();

        X500Name subject = new X500Name("CN=Test Manifest Signing");

        X509v3CertificateBuilder builder =
                new JcaX509v3CertificateBuilder(
                        subject,
                        BigInteger.ONE,
                        Date.from(now.minusSeconds(60)),
                        Date.from(now.plusSeconds(3600)),
                        subject,
                        keyPair.getPublic()
                );

        ContentSigner contentSigner =
                new JcaContentSignerBuilder(signatureAlgorithm)
                        .build(keyPair.getPrivate());

        X509CertificateHolder certificateHolder =
                builder.build(contentSigner);

        return new JcaX509CertificateConverter()
                .getCertificate(certificateHolder);
    }
}
