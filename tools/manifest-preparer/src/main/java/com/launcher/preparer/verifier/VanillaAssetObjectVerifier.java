package com.launcher.preparer.verifier;

import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class VanillaAssetObjectVerifier {

    public VerifiedVanillaAssetObject verify(
            VanillaAssetObject expected,
            InputStream input
    ) throws IOException {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(input, "input");

        MessageDigest sha1 = digest("SHA-1");
        MessageDigest sha256 = digest("SHA-256");

        byte[] buffer = new byte[8192];
        long totalSize = 0;
        int count;

        while ((count = input.read(buffer)) != -1) {
            if (count > expected.size() - totalSize) {
                throw new VanillaAssetObjectIntegrityException(
                        "Asset size mismatch"
                );
            }

            totalSize += count;
            sha1.update(buffer, 0, count);
            sha256.update(buffer, 0, count);
        }

        if (totalSize != expected.size()) {
            throw new VanillaAssetObjectIntegrityException(
                    "Asset size mismatch"
            );
        }

        String actualSha1 = HexFormat.of().formatHex(sha1.digest());

        if (!expected.sha1().equals(actualSha1)) {
            throw new VanillaAssetObjectIntegrityException(
                    "Asset SHA-1 mismatch"
            );
        }

        return new VerifiedVanillaAssetObject(
                expected,
                HexFormat.of().formatHex(sha256.digest())
        );
    }

    private static MessageDigest digest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                   algorithm + " is unavailable",
                    e
            );
        }
    }
}
