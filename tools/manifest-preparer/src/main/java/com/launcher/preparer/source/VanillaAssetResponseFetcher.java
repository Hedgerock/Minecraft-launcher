package com.launcher.preparer.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

@FunctionalInterface
interface VanillaAssetResponseFetcher {

    Response fetch(URI uri) throws IOException, InterruptedException;

    record Response(
            int statusCode,
            InputStream body
    ) {

        public Response {
            Objects.requireNonNull(body, "body");
        }
    }
}
