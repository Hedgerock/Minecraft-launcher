package com.launcher.testing.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LocalHttpServer implements AutoCloseable {

    private static final String LOOPBACK_HOST = "127.0.0.1";

    private final HttpServer server;
    private final List<PausedHttpResponse> pausedResponses = new ArrayList<>();
    private boolean closed;

    private LocalHttpServer(HttpServer server) {
        this.server = server;
    }

    public static LocalHttpServer bind() throws IOException {
        return new LocalHttpServer(
                HttpServer.create(
                        new InetSocketAddress(LOOPBACK_HOST, 0),
                        0
                )
        );
    }

    public void start() {
        ensureOpen();
        server.start();
    }

    public URI uri(String path) {
        Objects.requireNonNull(path, "path");

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("HTTP test path must start with '/'");
        }

        return URI.create("http://" + LOOPBACK_HOST + ":" + server.getAddress().getPort() + path);
    }

    public void createContext(String path, HttpHandler handler) {
        ensureOpen();
        server.createContext(path, handler);
    }

    public PausedHttpResponse registerPausedBody(
            String path,
            int status,
            byte[] prefix,
            Duration releaseTimeout
    ) {
        return registerPausedBody(path, status, prefix, new byte[0], releaseTimeout);
    }

    public PausedHttpResponse registerPausedBody(
            String path,
            int status,
            byte[] prefix,
            byte[] suffix,
            Duration releaseTimeout
    ) {
        PausedHttpResponse response = new PausedHttpResponse(status, prefix, suffix, releaseTimeout);
        createContext(path, response::handle);
        pausedResponses.add(response);

        return response;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        pausedResponses.forEach(PausedHttpResponse::release);
        server.stop(0);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("HTTP test server is closed");
        }
    }
}
