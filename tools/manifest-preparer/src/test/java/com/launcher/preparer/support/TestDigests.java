package com.launcher.preparer.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class TestDigests {

    private TestDigests() {
    }

    public static String sha1(byte[] bytes) {
        return hash("SHA-1", bytes);
    }

    public static String sha256(byte[] bytes) {
        return hash("SHA-256", bytes);
    }

    private static String hash(String algorithm, byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");

        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(bytes);

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError(
                    algorithm + " must be available in the test runtime",
                    exception
            );
        }
    }
}
