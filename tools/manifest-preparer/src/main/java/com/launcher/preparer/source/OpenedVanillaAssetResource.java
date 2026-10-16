package com.launcher.preparer.source;

import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

public final class OpenedVanillaAssetResource implements Closeable {

    private final URI uri;
    private final InputStream input;

    public OpenedVanillaAssetResource(
            URI uri,
            InputStream input
    ) {
        this.uri = VanillaAssetResourceUriValidator.requireValid(uri);
        this.input = Objects.requireNonNull(input, "input");
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getInput() {
        return input;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
