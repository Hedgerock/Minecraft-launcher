package com.launcher.publisher;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

public record ExportPublicKey(
        Path keyStoreFile,
        String alias,
        Path publicKeyFile
) implements ManifestPublisherCommand {

    @Override
    public void execute(
            LocalManifestPublicationPreparer preparer,
            char[] password
    ) throws IOException, GeneralSecurityException {
        preparer.exportPublicKey(
                keyStoreFile,
                alias,
                password,
                publicKeyFile
        );
    }
}
