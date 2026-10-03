package com.launcher.api.manifest;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.HttpManifestSignatureClient;
import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.service.SignedHttpManifestService;
import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.api.manifest.signature.ManifestSignatureVerificationException;
import com.launcher.api.manifest.support.ManifestJsonProvider;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpManifestLoadingIntegrationTest {
    private static final String ALGORITHM = "Ed25519";

    @Test
    void should_reject_manifest_when_response_differs_from_signed_bytes() throws Exception {
        //given
        byte[] manifestBytes = ManifestJsonProvider.getMinimumValidManifestJson("1.12.2")
                .getBytes(StandardCharsets.UTF_8);
        byte[] anotherManifest = ManifestJsonProvider.getMinimumValidManifestJson("1.7.10")
                .getBytes(StandardCharsets.UTF_8);

        KeyPair keyPair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        byte[] signatureBytes = signatureBytes(keyPair, manifestBytes);

        server.createContext(
                "/manifest.json",
                exchange -> writeResponse(exchange, anotherManifest)
        );

        server.createContext(
                "/signature",
                exchange -> writeResponse(exchange, signatureBytes)
        );

        server.start();

        try {
            //when & then
            SignedHttpManifestService signedHttpManifestService = signedHttpManifestService(server, keyPair);
            ManifestSignatureVerificationException exception = assertThrows(
                    ManifestSignatureVerificationException.class,
                    signedHttpManifestService::loadManifest
            );

            assertEquals(
                    "Manifest signature is invalid",
                    exception.getMessage()
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void should_download_signed_manifest_and_map_it_to_domain_model()
            throws Exception {
        //given
        byte[] manifestBytes = ManifestJsonProvider.getMinimumValidManifestJson()
                .getBytes(StandardCharsets.UTF_8);

        KeyPair keyPair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        byte[] signatureBytes = signatureBytes(keyPair, manifestBytes);

        server.createContext(
                "/manifest.json",
                exchange -> writeResponse(exchange, manifestBytes)
        );

        server.createContext(
                "/signature",
                exchange -> writeResponse(exchange, signatureBytes)
        );

        server.start();

        try {
            //when
            SignedHttpManifestService signedHttpManifestService = signedHttpManifestService(server, keyPair);
            ManifestLoadResult result = signedHttpManifestService.loadManifest();

            //then
            assertVerifyManifest(result.manifest());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void should_load_manifest_from_http_and_map_to_domain_model() throws Exception {
        //given
        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        byte[] manifestBytes = ManifestJsonProvider.getMinimumValidManifestJson()
                .getBytes(StandardCharsets.UTF_8);

        server.createContext(
                "/manifest.json",
                exchange -> writeResponse(exchange, manifestBytes)

        );

        server.start();

        try {
            MapperAndClient mapperAndClient = prepareMapperAndClient(server);
            var manifestClient = mapperAndClient.client();
            var manifestMapper = mapperAndClient.mapper();

            //when
            String json = manifestClient.download();
            Manifest manifest = manifestMapper.map(json).manifest();

            //then
            assertVerifyManifest(manifest);
        } finally {
            server.stop(0);
        }
    }

    private MapperAndClient prepareMapperAndClient(HttpServer server) {
        URI manifestUri = getCurrentUri(server, "/manifest.json");

        JavaLauncherHttpClient httpClient = new JavaLauncherHttpClient();
        HttpManifestClient manifestClient = new HttpManifestClient(httpClient, manifestUri);
        JsonManifestMapper manifestMapper = new JsonManifestMapper(
                new DefaultRuntimeLibrarySelector(),
                () -> new RuntimeEnvironment(OperatingSystem.WINDOWS)
        );

        return new MapperAndClient(manifestClient, manifestMapper);
    }

    private void writeResponse(
            HttpExchange exchange,
            byte[] response
    ) throws IOException {
        exchange.sendResponseHeaders(
                200,
                response.length
        );

        try (exchange; OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private SignedHttpManifestService signedHttpManifestService(
            HttpServer server,
            KeyPair keyPair
    ) {
        URI manifestUri = getCurrentUri(server, "/manifest.json");
        URI signatureUri = getCurrentUri(server, "/signature");

        JavaLauncherHttpClient httpClient = new JavaLauncherHttpClient();

        return new SignedHttpManifestService(
                new HttpManifestClient(
                        httpClient,
                        manifestUri
                ),
                new HttpManifestSignatureClient(
                        httpClient,
                        signatureUri
                ),
                new Ed25519ManifestSignatureVerifier(
                        keyPair.getPublic()
                ),
                new JsonManifestMapper(
                        new DefaultRuntimeLibrarySelector(),
                        () ->
                                new RuntimeEnvironment(OperatingSystem.WINDOWS)

                )
        );
    }

    private URI getCurrentUri(HttpServer server, String endPoint) {
        return URI.create("http://localhost:" + server.getAddress().getPort() + endPoint);
    }

    private byte[] signatureBytes(KeyPair keyPair, byte[] manifestBytes) throws Exception {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(keyPair.getPrivate());
        signer.update(manifestBytes);

        return signer.sign();
    }

    private void assertVerifyManifest(
            Manifest manifest
    ) {
        assertEquals("1.12.2", manifest.minecraftVersion());
        assertEquals("fabric", manifest.loader().type());
        assertEquals("0.16.10", manifest.loader().version());
        assertEquals("net.minecraft.client.main.Main", manifest.launchInfo().mainClass());
        assertEquals(List.of("libraries/org/example/example.jar"),
                manifest.libraries()
                        .stream()
                        .map(LibraryEntry::path)
                        .toList());
    }

    private record MapperAndClient(
            HttpManifestClient client,
            JsonManifestMapper mapper
    ) {}
}
