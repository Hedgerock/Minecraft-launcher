package com.launcher.preparer.source;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.preparer.VanillaAssetIndexReader;
import com.launcher.preparer.VanillaAssetObjectsPreparer;
import com.launcher.preparer.manifest.ManagedManifestAssetAssembler;
import com.launcher.preparer.mapper.VanillaAssetsMapper;
import com.launcher.preparer.model.PreparedVanillaAssetIndex;
import com.launcher.preparer.model.PreparedVanillaAssetObject;
import com.launcher.preparer.verifier.VanillaAssetObjectIntegrityException;
import com.launcher.preparer.verifier.VanillaAssetObjectVerifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaAssetPreparationIntegrationTest {

    private static final String INDEX_ID = "test-assets";
    private static final URI INDEX_URI = URI.create(
            "https://indexes.example.test/source.json?revision=7"
    );
    private static final URI OBJECTS_BASE_URI = URI.create(
            "https://assets.example.test/releases/objects/"
    );

    @Test
    void should_assemble_managed_manifest_from_prepared_assets() throws IOException {
        //given
        byte[] first = bytes("first-content");
        byte[] second = bytes("second-content");

        byte[] indexBytes = indexBytes(first, first, second);

        URI firstUri = objectUri(first);
        URI secondUri = objectUri(second);

        RecordingFetcher fetcher = new RecordingFetcher(
                Map.of(
                        INDEX_URI, indexBytes,
                        firstUri, first,
                        secondUri, second
                )
        );

        byte[] template = bytes("""
                {
                    "minecraftVersion": "test-version",
                    "loader": null,
                    "files": [],
                    "launchInfo": {
                        "mainClass": "example.Main",
                        "javaExecutable": "java",
                        "jvmArgs": ["-Xmx1G"],
                        "gameArgs": ["--username", "Player"]
                    },
                    "libraries": [
                        {
                            "path": "libraries/example.jar",
                            "sha256": "535436571185ff3d2564aceead2dfece7bf7b69fc80ea0174a508b133110af2e",
                            "size": 123,
                            "url": "https://libraries.example.test/example.jar",
                            "rules": [
                                {"action": "allow", "os": "windows"}
                            ]
                        }
                    ],
                    "metadata": {
                        "title": "Keystone — проверка",
                        "enabled": true,
                        "note": null
                    },
                    "assets": [
                        {"path": "assets/obsolete.bin"}
                    ],
                    "assetIndex": {
                        "id": "old-index",
                        "legacy": true
                    }
                }
                """
        );

        ObjectMapper json = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        ObjectNode expected = (ObjectNode) json.readTree(template);
        ArrayNode expectedAssets = expected.putArray("assets");

        for (byte[] content : List.of(first, second)) {
            AssetEntry asset = expectedAsset(content, objectUri(content));

            expectedAssets.addObject()
                    .put("path", asset.path())
                    .put("sha256", asset.sha256())
                    .put("size", asset.size())
                    .put("url", asset.url());
        }

        expected.putObject("assetIndex")
                .put("id", INDEX_ID)
                .put("path", "assets/indexes/test-assets.json")
                .put("sha256", hash("SHA-256", indexBytes))
                .put("size", indexBytes.length)
                .put("url", INDEX_URI.toString());

        ManagedManifestAssetAssembler assembler =
                new ManagedManifestAssetAssembler();

        //when
        AssetsIndex preparedAssets = prepareAssets(fetcher, indexBytes);
        byte[] result = assembler.assemble(template, preparedAssets);

        //then
        assertEquals(
                json.readTree(json.writeValueAsBytes(expected)),
                json.readTree(result)
        );

        assertEquals(
                List.of(INDEX_URI, firstUri, secondUri),
                fetcher.requestedUris
        );

        assertEquals(3, fetcher.streams.size());

        assertTrue(
                fetcher.streams.stream().allMatch(TrackingInputStream::wasClosed)
        );
    }

    @Test
    void should_stop_before_loading_objects_when_index_is_corrupt() {
        //given
        byte[] first = bytes("first-content");
        byte[] second = bytes("second-content");

        byte[] indexBytes = indexBytes(first, second);
        byte[] corruptedIndexBytes = indexBytes.clone();
        corruptedIndexBytes[corruptedIndexBytes.length - 1] = (byte) ' ';

        RecordingFetcher fetcher = new RecordingFetcher(
                Map.of(
                        INDEX_URI, corruptedIndexBytes,
                        objectUri(first), first,
                        objectUri(second), second
                )
        );

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> prepareAssets(fetcher, indexBytes)
        );

        assertEquals(
                List.of(INDEX_URI),
                fetcher.requestedUris
        );

        assertEquals(1, fetcher.streams.size());

        assertTrue(
                fetcher.streams.stream().allMatch(TrackingInputStream::wasClosed)
        );
    }

    @Test
    void should_prepare_complete_manifest_asset_resources() throws IOException {
        //given
        byte[] first = bytes("first-content");
        byte[] second = bytes("second-content");

        byte[] indexBytes = indexBytes(first, first, second);

        URI firstUri = objectUri(first);
        URI secondUri = objectUri(second);

        RecordingFetcher fetcher = new RecordingFetcher(
                Map.of(
                        INDEX_URI, indexBytes,
                        firstUri, first,
                        secondUri, second
                )
        );

        //when
        AssetsIndex result = prepareAssets(fetcher, indexBytes);

        //then
        assertEquals(
                new AssetIndexMetadata(
                        INDEX_ID,
                        "assets/indexes/test-assets.json",
                        hash("SHA-256", indexBytes),
                        indexBytes.length,
                        INDEX_URI.toString()
                ),
                result.index()
        );

        assertEquals(
                List.of(INDEX_URI, firstUri, secondUri),
                fetcher.requestedUris
        );

        assertEquals(
                List.of(
                        expectedAsset(first, firstUri),
                        expectedAsset(second, secondUri)
                ),
                result.assets()
        );

        assertEquals(3, fetcher.streams.size());
        assertTrue(fetcher.streams.stream().allMatch(TrackingInputStream::wasClosed));
    }

    @Test
    void should_stop_preparation_when_asset_object_is_corrupt() {
        //given
        byte[] first = bytes("first-content");
        byte[] second = bytes("second-content");
        byte[] third = bytes("third-content");

        byte[] indexBytes = indexBytes(first, second, third);

        byte[] corruptedSecond = second.clone();
        corruptedSecond[0] ^= 1;

        URI firstUri = objectUri(first);
        URI secondUri = objectUri(second);
        URI thirdUri = objectUri(third);

        RecordingFetcher fetcher = new RecordingFetcher(
                Map.of(
                        INDEX_URI, indexBytes,
                        firstUri, first,
                        secondUri, corruptedSecond,
                        thirdUri, third
                )
        );

        //when & then
        assertThrows(
                VanillaAssetObjectIntegrityException.class,
                () -> prepareAssets(fetcher, indexBytes)
        );

        assertEquals(
                List.of(INDEX_URI, firstUri, secondUri),
                fetcher.requestedUris
        );

        assertEquals(3, fetcher.streams.size());

        assertTrue(
                fetcher.streams.stream().allMatch(TrackingInputStream::wasClosed)
        );
    }

    private AssetsIndex prepareAssets(
            RecordingFetcher fetcher,
            byte[] expectedIndexBytes
    ) throws IOException {
        HttpVanillaAssetResourceSource resourceSource =
                new HttpVanillaAssetResourceSource(fetcher);

        HttpVanillaAssetIndexLoader loader =
                new HttpVanillaAssetIndexLoader(
                        resourceSource,
                        new VanillaAssetIndexReader()
                );

        PreparedVanillaAssetIndex index = loader.load(
                INDEX_URI,
                hash("SHA-1", expectedIndexBytes),
                expectedIndexBytes.length
        );

        VanillaAssetObjectsPreparer preparer =
                new VanillaAssetObjectsPreparer(
                        new HttpVanillaAssetObjectSource(
                                OBJECTS_BASE_URI,
                                resourceSource
                        ),
                        new VanillaAssetObjectVerifier()
                );

        List<PreparedVanillaAssetObject> objects = preparer.prepare(index.verified().index());

        return new VanillaAssetsMapper().map(
                INDEX_ID,
                index,
                objects
        );
    }

    private static AssetEntry expectedAsset(byte[] content, URI uri) {
        String sha1 = hash("SHA-1", content);

        return new AssetEntry(
                "assets/objects/" + sha1.substring(0, 2) + "/" + sha1,
                hash("SHA-256", content),
                content.length,
                uri.toString()
        );
    }

    private static URI objectUri(byte[] content) {
        String sha1 = hash("SHA-1", content);

        return URI.create(
                "https://assets.example.test/releases/objects/"
                    + sha1.substring(0, 2)
                    + "/"
                    + sha1
        );
    }

    private static byte[] indexBytes(byte[]... objects) {
        String start = """
                    {"objects":{
                """;

        StringBuilder json = new StringBuilder(start);

        for (int i = 0; i < objects.length; i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append("\"resource-")
                    .append(i)
                    .append("\":{\"hash\":\"")
                    .append(hash("SHA-1", objects[i]))
                    .append("\",\"size\":")
                    .append(objects[i].length)
                    .append('}');
        }

        json.append("}}\n");

        return bytes(json.toString());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static String hash(String algorithm, byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm)
                    .digest(content);

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(
                    algorithm + " must be available",
                    e
            );
        }
    }

    private static final class RecordingFetcher implements VanillaAssetResponseFetcher {

        private final Map<URI, byte[]> resources;
        private final List<URI> requestedUris = new ArrayList<>();
        private final List<TrackingInputStream> streams = new ArrayList<>();

        public RecordingFetcher(Map<URI, byte[]> resources) {
            this.resources = resources;
        }

        @Override
        public Response fetch(URI uri) {
            requestedUris.add(uri);

            byte[] content = resources.get(uri);

            if (content == null) {
                throw new AssertionError("Unexpected resource URI: " + uri);
            }

            TrackingInputStream stream = new TrackingInputStream(content);
            streams.add(stream);

            return new VanillaAssetResponseFetcher.Response(200, stream);
        }
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        public TrackingInputStream(byte[] content) {
            super(content);
        }

        private boolean wasClosed() {
            return closed;
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
