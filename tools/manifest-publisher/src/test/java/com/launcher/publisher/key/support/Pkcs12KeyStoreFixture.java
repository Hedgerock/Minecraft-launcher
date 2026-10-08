package com.launcher.publisher.key.support;

import com.launcher.publisher.manifest.support.ManifestSignerFixture;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

public final class Pkcs12KeyStoreFixture {
    private static final ManifestSignerFixture manifestSignerFixture =
            new ManifestSignerFixture();

    private Pkcs12KeyStoreFixture() {
    }

    public static void create(
            Path keyStoreFile,
            String alias,
            char[] password
    ) throws Exception {
        KeyPair keyPair = manifestSignerFixture.generateKeyPair();
        createKeyStore(keyStoreFile, alias, password, keyPair);
    }

    public static void createKeyStore(
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

    public static void createKeyStore(
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

    public static X509Certificate createCertificate(KeyPair keyPair) throws Exception {
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
