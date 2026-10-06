package com.launcher.downloader.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

@FunctionalInterface
interface ResponseFetcher {
    record Response(
            int statusCode,
            InputStream body
    ) {

        public Response {
            Objects.requireNonNull(body, "body");
        }
    }

    Response fetch(URI uri) throws IOException, InterruptedException;
}
