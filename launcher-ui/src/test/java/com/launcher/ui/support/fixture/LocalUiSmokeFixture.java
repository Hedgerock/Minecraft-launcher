package com.launcher.ui.support.fixture;

import com.launcher.testing.process.FakeJavaExecutableFixture;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class LocalUiSmokeFixture implements AutoCloseable {
    private final URI manifestUri;
    private final Path launcherDirectory;
    private final Path gameStartedMarker;
    private final HttpServer httpServer;

    private final CountDownLatch manifestRequested = new CountDownLatch(1);
    private final CountDownLatch manifestReleased = new CountDownLatch(1);

    private LocalUiSmokeFixture(Path tempDir) throws IOException {
        launcherDirectory = tempDir.resolve("launcher-directory");
        gameStartedMarker = tempDir.resolve("gamestarted.marker");

        Path executable = FakeJavaExecutableFixture.create(tempDir, gameStartedMarker);
        String manifest = getManifestJson(executable.toAbsolutePath().toString());
        String path = "/manifest.json";

        httpServer = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        createJsonContext(httpServer, path, manifest);
        httpServer.start();

        manifestUri = URI.create(
                "http://127.0.0.1:" + httpServer.getAddress().getPort() + path
        );
    }

    public static LocalUiSmokeFixture start(Path tempDir) throws IOException {
        return new LocalUiSmokeFixture(tempDir);
    }

    public void releaseManifest() {
        manifestReleased.countDown();
    }

    public boolean awaitManifestRequest(Duration timeout) throws InterruptedException {
        return manifestRequested.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean awaitGameStartedMarker(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (Files.exists(gameStartedMarker)) {
                return true;
            }

            Thread.sleep(25);
        }

        return Files.exists(gameStartedMarker);
    }

    public URI getManifestUri() {
        return manifestUri;
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    @Override
    public void close() {
        releaseManifest();
        httpServer.stop(0);
    }

    private static String getManifestJson(String javaExecutable) {
        String escapedExecutable = javaExecutable
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String template = """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": %s,
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                        "gameArgs": ["--version", "${version_name}"],
                        "classpath": ["versions/client.jar"],
                        "javaExecutable": "%s",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 17
                        }
                    },
                    "libraries": [],
                    "assets": %s
                }
                """;

        return template.formatted("[]", escapedExecutable, "[]");
    }

    private void createJsonContext(HttpServer server, String path, String json) {
        server.createContext(path, exchange -> {
            try (exchange) {
                manifestRequested.countDown();

                try {
                    if (!manifestReleased.await(15, TimeUnit.SECONDS)) {
                        exchange.sendResponseHeaders(503, -1);
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exchange.sendResponseHeaders(503, -1);
                    return;
                }

                byte[] response = json.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(response);
                }
            }
        });
    }
}
