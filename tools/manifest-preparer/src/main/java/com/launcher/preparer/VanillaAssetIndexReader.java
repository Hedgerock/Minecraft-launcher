package com.launcher.preparer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class VanillaAssetIndexReader {

    private static final Pattern SHA1_PATTERN = Pattern.compile("^[0-9a-f]{40}$");
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public VanillaAssetIndex read(
            byte[] indexBytes,
            String expectedSha1,
            long expectedSize
    ) {
        validateArguments(indexBytes, expectedSha1, expectedSize);

        final JsonNode root;

        try {
            root = MAPPER.readTree(indexBytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Invalid asset index JSON",
                    e
            );
        }

        validateAssetIndex(root, "root");
        JsonNode objects = validateAssetIndex(root.get("objects"), "objects");

        Map<String, VanillaAssetObject> uniqueObjects = new LinkedHashMap<>();

        for (Map.Entry<String, JsonNode> entry : objects.properties()) {
            JsonNode value = entry.getValue();

            if (!value.isObject()) {
                throw  new IllegalArgumentException(
                        "Invalid asset object: " + entry.getKey()
                );
            }

            JsonNode hashNode = value.get("hash");
            JsonNode sizeNode = value.get("size");

            if (
                hashNode == null
                || !hashNode.isTextual()
                || !SHA1_PATTERN.matcher(hashNode.textValue()).matches()
            ) {
                throw new IllegalArgumentException(
                        "Invalid asset hash: " + entry.getKey()
                );
            }

            if (
                sizeNode == null
                || !sizeNode.canConvertToLong()
                || !sizeNode.isIntegralNumber()
                || sizeNode.longValue() < 0
            ) {
                throw new IllegalArgumentException(
                        "Invalid asset size: " + entry.getKey()
                );
            }

            String hash = hashNode.textValue();
            long size = sizeNode.longValue();
            VanillaAssetObject previous = uniqueObjects.putIfAbsent(
                    hash,
                    new VanillaAssetObject(hash, size)
            );

            if (previous != null && previous.size() != size) {
                throw new IllegalArgumentException(
                        "Conflicting sizes for asset hash: " + hash
                );
            }
        }

        return new VanillaAssetIndex(
                new ArrayList<>(uniqueObjects.values())
        );
    }

    private JsonNode validateAssetIndex(
            JsonNode candidate,
            String jsonName
    ) {
        if (candidate == null || !candidate.isObject()) {
            throw new IllegalArgumentException(
                    "Asset index %s must be an object".formatted(jsonName)
            );
        }

        return candidate;
    }

    private void validateArguments(
            byte[] indexBytes,
            String expectedSha1,
            long expectedSize
    ) {
        Objects.requireNonNull(indexBytes, "indexBytes");
        Objects.requireNonNull(expectedSha1, "expectedSha1");

        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize must not be negative");
        }

        if (!SHA1_PATTERN.matcher(expectedSha1).matches()) {
            throw new IllegalArgumentException(
                    "expectedSha1 must be a lowercase SHA-1"
            );
        }

        if (indexBytes.length != expectedSize) {
            throw new IllegalArgumentException("Asset index size does not match metadata");
        }

        if (!sha1(indexBytes).equals(expectedSha1)) {
            throw new IllegalArgumentException(
                    "Asset index SHA-1 does not match metadata"
            );
        }
    }

    private static String sha1(byte[] bytes) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-1").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is unavailable");
        }
    }
}
