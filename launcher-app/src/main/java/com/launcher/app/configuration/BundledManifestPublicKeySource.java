package com.launcher.app.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import java.util.function.Supplier;

public final class BundledManifestPublicKeySource {
    private static final String ALGORITHM = "Ed25519";

    private final Supplier<InputStream> publicKeyResolver;

    BundledManifestPublicKeySource(Supplier<InputStream> publicKeyResolver) {
        this.publicKeyResolver = Objects.requireNonNull(
                publicKeyResolver,
                "publicKeyResolver"
        );
    }

    public BundledManifestPublicKeySource() {
        this(() -> BundledManifestPublicKeySource.class.getResourceAsStream(
                "/managed-manifest-public-key.der"
        ));
    }

    public PublicKey load() {
        try (InputStream inputStream = publicKeyResolver.get()) {
            if (inputStream == null) {
                throw new ManifestPublicKeyConfigurationException(
                        "Failed to load public key from bundled resource"
                );
            }

            byte[] rawBytes = inputStream.readAllBytes();

            KeyFactory keyFactory =
                    KeyFactory.getInstance(ALGORITHM);

            return keyFactory.generatePublic(
                    new X509EncodedKeySpec(rawBytes)
            );
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ManifestPublicKeyConfigurationException(
                    "Failed to load public key from bundled resource",
                    e
            );
        }
    }
}
