package com.launcher.preparer.source;

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
        this.uri = requireResourceUri(uri);
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

    private static URI requireResourceUri(URI uri) {
        Objects.requireNonNull(uri, "uri");

        if (
                !uri.isAbsolute()
                || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getRawUserInfo() != null
                || uri.getRawFragment() != null
        ) {
            throw new IllegalArgumentException(
                    "Resource URI must be an absolute HTTPS URI with a " +
                            "host and without user info or fragment"
            );
        }

        return uri;
    }
}
