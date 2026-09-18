package com.launcher.app.support;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public final class LocalServerStarter {

    public void createOctetStreamContext(
            HttpServer server,
            String path,
            AtomicInteger resourceRequests,
            byte[] content
    ) {

        server.createContext(path, exchange -> {
            resourceRequests.incrementAndGet();

            exchange.getResponseHeaders()
                    .add("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, content.length);

            try (exchange; OutputStream body = exchange.getResponseBody()) {
                body.write(content);
            }
        });
    }

    public void createJsonContext(HttpServer server, String path, String json) {
        server.createContext(path, exchange -> {
            byte[] response = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);

            try (exchange; OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response);
            }
        });
    }

    public HttpServer getServer(
            String json,
            String path
    ) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        createJsonContext(server, path, json);

        return server;
    }
}
