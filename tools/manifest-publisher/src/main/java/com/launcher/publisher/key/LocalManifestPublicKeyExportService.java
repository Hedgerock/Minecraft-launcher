package com.launcher.publisher.key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public final class LocalManifestPublicKeyExportService {
    private static final String ALGORITHM = "Ed25519";

    public void export(
            PublicKey publicKey,
            Path outputFile
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(publicKey, "publicKey");
        Objects.requireNonNull(outputFile, "outputFile");

        byte[] encoded = publicKey.getEncoded();

        if (!"X.509".equals(publicKey.getFormat())
                || encoded == null
                || encoded.length == 0
        ) {
            throw new InvalidKeyException(
                    "Public key has no SubjectPublicKeyInfo encoding"
            );
        }

        KeyFactory.getInstance(ALGORITHM).generatePublic(
                new X509EncodedKeySpec(encoded)
        );

        Files.write(
                outputFile,
                encoded,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
    }
}
