package com.launcher.publisher.manifest.support;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public final class ManifestSignerFixture {
    public static final String DEFAULT_MANIFEST_VALUE = "Manifest value";

    public KeyPair generateKeyPair() throws GeneralSecurityException {
        return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    }

    public KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public byte[] getManifestBytes() {
        return DEFAULT_MANIFEST_VALUE.getBytes(StandardCharsets.UTF_8);
    }
}
