package com.launcher.storage.hash;

import com.launcher.core.storage.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Sha256HashService implements HashService {
    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;
    private static final String STORAGE_EXCEPTION_TEMPLATE = "Unable to calculate SHA-256 hash for file: %s";

    @Override
    public String sha256(Path filePath) {

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            try (InputStream inputStream = Files.newInputStream(filePath)) {

                byte[] buffer = new byte[BUFFER_SIZE];

                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }

            }

            return HexFormat.of().formatHex(digest.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new StorageException(
                    STORAGE_EXCEPTION_TEMPLATE.formatted(filePath),
                    e
            );
        }

    }

}
