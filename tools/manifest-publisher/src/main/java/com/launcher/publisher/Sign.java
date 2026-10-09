package com.launcher.publisher;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

public record Sign(
        Path keyStoreFile,
        String alias,
        Path manifestFile,
        Path signatureFile
) implements ManifestPublisherCommand {
    @Override
    public void execute(
            LocalManifestPublicationPreparer preparer,
            char[] password
    ) throws IOException, GeneralSecurityException {
        preparer.signManifest(
                keyStoreFile,
                alias,
                password,
                manifestFile,
                signatureFile
        );
    }
}
