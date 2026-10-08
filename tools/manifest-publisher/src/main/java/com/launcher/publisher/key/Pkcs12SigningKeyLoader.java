package com.launcher.publisher.key;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Objects;

public final class Pkcs12SigningKeyLoader {
    private static final String ALGORITHM = "Ed25519";
    private static final String KEY_STORE_TYPE = "PKCS12";

    public PrivateKey load(
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

        Signature.getInstance(ALGORITHM).initSign(privateKey);

        return privateKey;
    }
}
