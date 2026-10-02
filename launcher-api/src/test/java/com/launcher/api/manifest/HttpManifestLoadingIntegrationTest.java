package com.launcher.api.manifest;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.support.ManifestJsonProvider;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
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
            byte[] response = ManifestJsonProvider.getMinimumValidManifestJson()
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response);
            }
        });

        server.start();

        try {
            MapperAndClient mapperAndClient = prepareMapperAndClient(server);
            var manifestClient = mapperAndClient.client();
            var manifestMapper = mapperAndClient.mapper();

            //when
            String json = manifestClient.download();
            Manifest manifest = manifestMapper.map(json).manifest();

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

    private MapperAndClient prepareMapperAndClient(HttpServer server) throws Exception {
        URI manifestUri = new URI("http://localhost:" + server.getAddress().getPort() + "/manifest.json");

        JavaLauncherHttpClient httpClient = new JavaLauncherHttpClient();
        HttpManifestClient manifestClient = new HttpManifestClient(httpClient, manifestUri);
        JsonManifestMapper manifestMapper = new JsonManifestMapper(
                new DefaultRuntimeLibrarySelector(),
                () -> new RuntimeEnvironment(OperatingSystem.WINDOWS)
        );

        return new MapperAndClient(manifestClient, manifestMapper);
    }

    private record MapperAndClient(
            HttpManifestClient client,
            JsonManifestMapper mapper
    ) {}
}
