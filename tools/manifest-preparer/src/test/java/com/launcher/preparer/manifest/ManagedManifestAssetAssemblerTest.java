package com.launcher.preparer.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedManifestAssetAssemblerTest {

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    private static final String FIRST_SHA256 = "b".repeat(64);
    private static final String SECOND_SHA256 = "c".repeat(64);
    private static final String INDEX_SHA256 = "d".repeat(64);

    private final ManagedManifestAssetAssembler assembler =
            new ManagedManifestAssetAssembler();

    @Test
    void should_add_prepared_assets_when_template_has_no_asset_fields() throws IOException {
        //given
        byte[] template = bytes("{}");
        AssetsIndex assets = assetsIndex();

        //when
        byte[] result = assembler.assemble(template, assets);

        //then
        assertEquals(expectedAssetFields(), read(result));
    }

    @Test
    void should_replace_existing_asset_fields_without_merging() throws IOException {
        //given
        byte[] template = bytes("""
                {
                    "minecraftVersion": "test-version",
                    "assets": [
                        {
                            "path": "assets/obsolete.bin"
                        }
                    ],
                    "assetIndex": {
                        "id": "old-index",
                        "legacy": true
                    }
                }
                """
        );

        ObjectNode expected = expectedAssetFields();
        expected.put("minecraftVersion", "test-version");

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        assertEquals(expected, read(result));
    }

    @Test
    void should_preserve_other_manifest_sections() throws IOException {
        //given
        byte[] template = bytes("""
                {
                    "minecraftVersion": "test-version",
                    "loader": null,
                    "files": [
                        {
                            "path": "versions/test/client.jar",
                            "marker": "client-metadata"
                        }
                    ],
                    "launchInfo": {
                        "mainClass": "example.Main",
                        "javaExecutable": "java",
                        "gameArgs": ["--username", "Player"]
                    },
                    "libraries": [
                        {
                            "path": "libraries/example.jar",
                            "rules": [
                                {"action": "allow", "os": "windows"},
                                {"action": "disallow", "os": "linux"}
                            ],
                            "classifiers": {
                                "natives-windows": {
                                    "path": "libraries/native.jar",
                                    "size": 10
                                }
                            },
                            "natives": {
                                "windows": "natives-windows"
                            },
                            "extract": {
                                "exclude": ["META-INF/", "licenses/"]
                            }
                        }
                    ],
                    "metadata": {
                        "enabled": true,
                        "note": null
                    }
                }
                """);

        JsonNode expected = read(template);

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        ObjectNode actual = (ObjectNode) read(result);

        actual.remove("assets");
        actual.remove("assetIndex");

        assertEquals(expected, actual);
    }

    @Test
    void should_preserve_array_element_order() throws IOException {
        //given
        byte[] template = bytes("""
                    {
                        "launchInfo": {
                            "gameArgs": [
                                "--username",
                                "Player",
                                "--version",
                                "test-version"
                            ]
                        },
                        "libraries": [
                            {"path": "libraries/z.jar"},
                            {"path": "libraries/a.jar"}
                        ]
                    }
                """);

        JsonNode expected = read(template);

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        JsonNode actual = read(result);

        assertEquals(
                expected.get("launchInfo").get("gameArgs"),
                actual.get("launchInfo").get("gameArgs")
        );

        assertEquals(
                expected.get("libraries"),
                actual.get("libraries")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0.12345678901234567890123456789",
            "12345678901234567890.12345678901234567890",
            "1.2300",
            "1e-40"
    })
    void should_preserve_decimal_precision(String value) throws IOException {
        //given
        byte[] template = bytes("{\"value\":" + value + "}");
        BigDecimal expected = new BigDecimal(value);

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        BigDecimal actual = read(result).get("value").decimalValue();

        assertEquals(0, expected.compareTo(actual));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9223372036854775808",
            "-9223372036854775809",
            "1234567890123456789012345678901234567890"
    })
    void should_preserve_large_integer_values(String value) throws IOException {
        //given
        byte[] template = bytes("{\"value\":" + value + "}");

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        assertEquals(
                new BigInteger(value),
                read(result).get("value").bigIntegerValue()
        );
    }

    @Test
    void should_preserve_unicode_values_in_utf8_output() throws IOException {
        //given
        String title = "Keystone — запуск 🚀";
        byte[] template = bytes("{\"title\":\"" + title + "\"}");

        //when
        byte[] result = assembler.assemble(template, assetsIndex());

        //then
        String decoded = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(result))
                .toString();

        assertEquals(
                title,
                JSON.readTree(decoded).get("title").textValue()
        );
    }

    @Test
    void should_not_modify_template_bytes() {
        //given
        byte[] template = bytes("""
                {
                    "assets": [],
                    "assetIndex": {"id": "old-index"},
                    "marker": "unchanged"
                }
                """
        );

        byte[] original = template.clone();

        //when
        assembler.assemble(template, assetsIndex());

        //then
        assertArrayEquals(original, template);
    }

    @Test
    void should_replace_existing_assets_with_empty_prepared_list() throws IOException {
        //given
        byte[] template = bytes("""
                {
                    "assets": [{ "path": "assets/obsolete.bin" }]
                }
                """
        );

        AssetsIndex assets = new AssetsIndex(
                List.of(),
                assetsIndex().index()
        );

        ObjectNode expected = expectedAssetFields();
        expected.set("assets", JSON.createArrayNode());

        //when
        byte[] result = assembler.assemble(template, assets);

        //then
        assertEquals(expected, read(result));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " \n\t",
            "null",
            "[]",
            "\"text\"",
            "42",
            "true",
            "{\"broken\":"
    })
    void should_reject_invalid_template(String json) {
        //given
        byte[] template = bytes(json);
        AssetsIndex assets = assetsIndex();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(template, assets)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"value\":1, \"value\":2}",
            "{\"section\":{\"value\":1, \"value\":2}}",
            "{\"items\":[{\"value\":1, \"value\":2}]}",
            "{\"assets\":[], \"assets\":[]}"
    })
    void should_reject_duplicate_field_names(String json) {
        //given
        byte[] template = bytes(json);
        AssetsIndex assets = assetsIndex();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(template, assets)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{} {}",
            "{} []",
            "{} null",
            "{} trailing"
    })
    void should_reject_trailing_json_content(String json) {
        //given
        byte[] template = bytes(json);
        AssetsIndex assets = assetsIndex();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(template, assets)
        );
    }

    @Test
    void should_preserve_template_parsing_failure_as_cause() {
        //given
        byte[] template = bytes("\"broken\":");

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(template, assetsIndex())
        );

        assertInstanceOf(
                JsonProcessingException.class,
                exception.getCause()
        );
    }

    @Test
    void should_reject_missing_asset_index_metadata() {
        //given
        byte[] template = bytes("{}");
        AssetsIndex assets = new AssetsIndex(List.of());

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(template, assets)
        );
    }

    @Test
    void should_reject_null_template_bytes() {
        //given
        AssetsIndex assets = assetsIndex();

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> assembler.assemble(null, assets)
        );
    }

    @Test
    void should_reject_null_assets() {
        //given
        byte[] template = bytes("{}");

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> assembler.assemble(template, null)
        );
    }

    private AssetsIndex assetsIndex() {
        return new AssetsIndex(
                List.of(
                        new AssetEntry(
                                "assets/first.bin",
                                FIRST_SHA256,
                                11,
                                "https://assets.example.test/first?revision=1"
                        ),
                        new AssetEntry(
                                "assets/second.bin",
                                SECOND_SHA256,
                                23,
                                "https://assets.example.test/second?revision=2"
                        )
                ),
                new AssetIndexMetadata(
                        "new-index",
                        "assets/indexes/new-index.json",
                        INDEX_SHA256,
                        512,
                        "https://indexes.example.test/source.json?revision=7"
                )
        );
    }

    private ObjectNode expectedAssetFields() throws IOException {
        return (ObjectNode) JSON.readTree("""
                {
                    "assets": [
                        {
                            "path": "assets/first.bin",
                            "sha256": "%s",
                            "size": 11,
                            "url": "https://assets.example.test/first?revision=1"
                        },
                        {
                            "path": "assets/second.bin",
                            "sha256": "%s",
                            "size": 23,
                            "url": "https://assets.example.test/second?revision=2"
                        }
                    ],
                    "assetIndex": {
                        "id": "new-index",
                        "path": "assets/indexes/new-index.json",
                        "sha256": "%s",
                        "size": 512,
                        "url": "https://indexes.example.test/source.json?revision=7"
                    }
                }
                """.formatted(FIRST_SHA256, SECOND_SHA256, INDEX_SHA256)
        );
    }

    private JsonNode read(byte[] bytes) throws IOException {
        return JSON.readTree(bytes);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
