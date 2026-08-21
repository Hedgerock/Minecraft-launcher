package com.launcher.api.manifest;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpManifestLoadingIntegrationTest {

    @Test
    void should_load_manifest_from_http_and_map_to_domain_model() throws Exception {
        //given
        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        server.createContext("/manifest.json", exchange -> {
            byte[] response = getManifestJson().getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response);
            }
        });

        server.start();

        try {
            URI manifestUri = new URI("http://localhost:" + server.getAddress().getPort() + "/manifest.json");

            JavaLauncherHttpClient httpClient = new JavaLauncherHttpClient();
            HttpManifestClient manifestClient = new HttpManifestClient(httpClient, manifestUri);
            JsonManifestMapper manifestMapper = new JsonManifestMapper();

            //when
            String json = manifestClient.download();
            Manifest manifest = manifestMapper.map(json);

            //then
            assertEquals("1.12.2", manifest.minecraftVersion());
            assertEquals("fabric", manifest.loader().type());
            assertEquals("0.16.10", manifest.loader().version());
            assertEquals("net.minecraft.client.main.Main", manifest.launchInfo().mainClass());
            assertEquals(List.of("libraries/org/example/example.jar"),
                    manifest.libraries()
                            .stream()
                            .map(LibraryEntry::path)
                            .toList());
        } finally {
            server.stop(0);
        }
    }

    private String getManifestJson() {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                        "gameArgs": ["--version", "${version_name}"],
                        "classpath": ["versions/client.jar"],
                        "javaExecutable": "java"
                    },
                    "libraries": [
                        {
                            "path": "libraries/org/example/example.jar",
                            "sha256": "library-sha256",
                            "size": 123,
                            "url": "https://example.com/libraries/org/example/example.jar"
                        }
                    ]
                }
                """;
    }
}
