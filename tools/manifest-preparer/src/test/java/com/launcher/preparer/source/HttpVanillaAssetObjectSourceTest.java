package com.launcher.preparer.source;

import com.launcher.preparer.model.VanillaAssetObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpVanillaAssetObjectSourceTest {

    @Test
    void should_return_response_body_with_resolved_asset_uri() throws IOException {
        //given
        String firstHash = "ab" + "1".repeat(38);
        String secondHash = "cd" + "2".repeat(38);

        URI baseUri = URI.create(
                "https://example.test/releases/assets/objects/"
        );

        URI firstUri = URI.create(
                "https://example.test/releases/assets/objects/ab/" + firstHash
        );

        URI secondUri = URI.create(
                "https://example.test/releases/assets/objects/cd/" + secondHash
        );

        InputStream firstInputStream = new ByteArrayInputStream(new byte[100]);
        InputStream secondInputStream = new ByteArrayInputStream(new byte[100]);

        Map<URI, InputStream> bodies = Map.of(
                firstUri, firstInputStream,
                secondUri, secondInputStream
        );

        List<URI> requestedUris = new ArrayList<>();

        VanillaAssetResponseFetcher fetcher = uri -> {
            requestedUris.add(uri);

            InputStream body = bodies.get(uri);

            if (body == null) {
                throw new IOException("Unexpected URI: " + uri);
            }

            return new VanillaAssetResponseFetcher.Response(200, body);
        };

        HttpVanillaAssetObjectSource objectSource =
                new HttpVanillaAssetObjectSource(
                        baseUri,
                        resourceSource(fetcher)
                );

        VanillaAssetObject first = vanillaAssetObject("ab", "1");
        VanillaAssetObject second = vanillaAssetObject("cd", "2");

        //when
        try (
                OpenedVanillaAssetResource firstResource = objectSource.open(first);
                OpenedVanillaAssetResource secondResource = objectSource.open(second)
        ) {
            //then
            assertEquals(List.of(firstUri, secondUri), requestedUris);

            assertEquals(firstUri, firstResource.getUri());
            assertEquals(secondUri, secondResource.getUri());

            assertSame(firstInputStream, firstResource.getInput());
            assertSame(secondInputStream, secondResource.getInput());
        }
    }

    @Test
    void should_preserve_failure_when_open_resource_failed() {
        //given
        URI uri = URI.create("https://example.org/path/value/");
        IOException failure = new IOException("Failed to open resource");
        VanillaAssetResponseFetcher fetcher = u -> {
            throw failure;
        };

        HttpVanillaAssetObjectSource source = new HttpVanillaAssetObjectSource(
                uri,
                resourceSource(fetcher)
        );

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open(vanillaAssetObject())
        );

        assertSame(failure, exception);
    }

    @ParameterizedTest(name = "should_reject_uri {0}")
    @MethodSource("insufficientArguments")
    void should_reject_insufficient_base_uri_criteria(
            String arg,
            URI uri
    ) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HttpVanillaAssetObjectSource(
                        uri,
                        resourceSource()
                )
        );

        String expectedMessage = "Base URI must end with '/' and must not contain a query";

        assertEquals(expectedMessage, exception.getMessage());
    }

    @ParameterizedTest(name = "should_reject_invalid_uri {0}")
    @MethodSource("com.launcher.preparer.support.InvalidUriProvider#invalidUris")
    void should_reject_invalid_uri(
            String arg,
            URI uri
    ) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HttpVanillaAssetObjectSource(
                        uri,
                        resourceSource()
                )
        );

        String expectedMessage = "Resource URI must be an absolute HTTPS URI with a " +
                "host and without user info or fragment";

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void should_reject_null_vanilla_asset_object() {
        //given
        HttpVanillaAssetObjectSource source = new HttpVanillaAssetObjectSource(
                URI.create("https://example.org/"),
                resourceSource()
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> source.open(null)
        );

        assertEquals("object", exception.getMessage());
    }

    @Test
    void should_reject_null_resource_source() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpVanillaAssetObjectSource(
                        URI.create("https://example.org/"),
                        null
                )
        );

        assertEquals("resourceSource", exception.getMessage());
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpVanillaAssetObjectSource(
                        null,
                        resourceSource()
                )
        );

        assertEquals("uri", exception.getMessage());
    }

    private HttpVanillaAssetResourceSource resourceSource() {
        return resourceSource(null);
    }

    private HttpVanillaAssetResourceSource resourceSource(
            VanillaAssetResponseFetcher fetcher
    ) {
        return fetcher == null
                ? (
                new HttpVanillaAssetResourceSource(uri -> {
                    throw new AssertionError("Unexpected uri: " + uri);
                })
                )
                : new HttpVanillaAssetResourceSource(fetcher);
    }

    private static Stream<Arguments> insufficientArguments() {
        return Stream.of(
                Arguments.of(
                        "without '/' at the end",
                        URI.create("https://example.org/test/not-valid")
                ),
                Arguments.of(
                        "with query",
                        URI.create("https://example.org/test/?query=value")
                ),
                Arguments.of(
                        "without path",
                        URI.create("https://example.org")
                )
        );
    }

    private VanillaAssetObject vanillaAssetObject() {
        return vanillaAssetObject("ab", "1");
    }

    private VanillaAssetObject vanillaAssetObject(String value, String hash) {
        return new VanillaAssetObject(
                value + hash.repeat(38),
                20
        );
    }
}
