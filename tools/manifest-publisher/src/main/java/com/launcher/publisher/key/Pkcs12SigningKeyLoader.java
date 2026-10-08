package com.launcher.publisher.key;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Objects;

public final class Pkcs12SigningKeyLoader {
    private static final String ALGORITHM = "Ed25519";
    private static final String KEY_STORE_TYPE = "PKCS12";

    public KeyPair load(
            Path keyStoreFile,
            String alias,
            char[] password
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(keyStoreFile, "keyStoreFile");
        Objects.requireNonNull(alias, "alias");
        Objects.requireNonNull(password, "password");

        if (alias.isBlank()) {
            throw new IllegalArgumentException("alias must not be blank");
        }

        if (password.length == 0) {
            throw new IllegalArgumentException("password must not be empty");
        }

        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);

        try (InputStream input = Files.newInputStream(keyStoreFile)) {
            keyStore.load(input, password);
        }

        Key key = keyStore.getKey(alias, password);

        if (!(key instanceof PrivateKey privateKey)) {
            throw new KeyStoreException(
                    "Alias does not contain a private signing key"
            );
        }

        Certificate certificate = keyStore.getCertificate(alias);

        if (certificate == null) {
            throw new KeyStoreException(
                    "Signing key has no public certificate"
            );
        }

        PublicKey publicKey = certificate.getPublicKey();
        verifyKeyPair(privateKey, publicKey);

        return new KeyPair(publicKey, privateKey);
    }

    private void verifyKeyPair(
            PrivateKey privateKey,
            PublicKey publicKey
    ) throws GeneralSecurityException {
        byte[] probe = "manifest-signing-key-pair"
                .getBytes(StandardCharsets.US_ASCII);

        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(privateKey);
        signer.update(probe);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance(ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(probe);

        if (!verifier.verify(signature)) {
            throw new KeyStoreException(
                    "Signing key does not match its public certificate"
            );
        }
    }
}
