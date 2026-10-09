package com.launcher.publisher;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Objects;

public sealed interface ManifestPublisherCommand permits Sign, ExportPublicKey {

    void execute(
            LocalManifestPublicationPreparer preparer,
            char[] password
    ) throws IOException, GeneralSecurityException;

    static ManifestPublisherCommand parse (String[] args) {
        Objects.requireNonNull(args, "args");

        if (args.length == 0 || args[0] == null) {
            throw usage();
        }

        return switch (args[0]) {
            case "sign" -> {
                if (args.length != 5) {
                    throw  usage();
                }

                yield new Sign(
                        path(args[1], "keystore"),
                        nonBlank(args[2], "alias"),
                        path(args[3], "manifest"),
                        path(args[4], "signature")
                );
            }

            case "export-public-key" -> {
                if (args.length != 4) {
                    throw usage();
                }

                yield new ExportPublicKey(
                        path(args[1], "keystore"),
                        nonBlank(args[2], "alias"),
                        path(args[3], "public key")
                );
            }

            default -> throw usage();
        };
    }

    private static Path path(String value, String name) {
        return Path.of(nonBlank(value, name));
    }

    private static String nonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name +  " must be non-blank");
        }

        return value;
    }

    private static IllegalArgumentException usage() {
        return new IllegalArgumentException(
                "Usage: sign <keystore.p12> <alias> <manifest.json> <manifest.sig>" +
                        " | export-public-key <keystore.p12> <alias> <public-key.der>"
        );
    }
}
