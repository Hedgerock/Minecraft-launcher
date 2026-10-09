package com.launcher.publisher;

import com.launcher.publisher.key.LocalManifestPublicKeyExportService;
import com.launcher.publisher.key.Pkcs12SigningKeyLoader;
import com.launcher.publisher.manifest.Ed25519ManifestSigner;
import com.launcher.publisher.manifest.LocalManifestSignatureService;

import java.io.Console;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public final class ManifestPublisherMain {

    private ManifestPublisherMain() {
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ManifestPublisherCommand command =
                ManifestPublisherCommand.parse(args);

        LocalManifestPublicationPreparer preparer =
                new LocalManifestPublicationPreparer(
                        new Pkcs12SigningKeyLoader(),
                        new LocalManifestSignatureService(
                                new Ed25519ManifestSigner()
                        ),
                        new LocalManifestPublicKeyExportService()
                );

        Console console = System.console();

        if (console == null) {
            throw new IllegalStateException(
                    "An interactive console is required"
            );
        }

        char[] password = console.readPassword("PKCS#12 password: ");

        if (password == null) {
            throw new IllegalStateException(
                    "Password input was cancelled"
            );
        }

        try {
            command.execute(preparer, password);
        } finally {
            Arrays.fill(password, '\0');
        }

        console.printf("Local publication artifact created.%n");
    }
}
