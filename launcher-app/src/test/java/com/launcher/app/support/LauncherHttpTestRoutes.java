package com.launcher.app.support;

import com.launcher.testing.http.LocalHttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public final class LauncherHttpTestRoutes {

    public void createJsonContext(LocalHttpServer server, String path, String json) {
        byte[] content = json.getBytes(StandardCharsets.UTF_8);
        server.createContext(path, exchange -> writeResponse(exchange, "application/json", content));
    }

    public void createOctetStreamContext(
            LocalHttpServer server,
            String path,
            AtomicInteger resourceRequests,
            byte[] content
    ) {
        server.createContext(path, exchange -> {
            resourceRequests.incrementAndGet();
            writeResponse(exchange, "application/octet-stream", content);
        });
    }

    private static void writeResponse(
            HttpExchange exchange,
            String contentType,
            byte[] content
    ) throws IOException {
        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
        }
    }
}
