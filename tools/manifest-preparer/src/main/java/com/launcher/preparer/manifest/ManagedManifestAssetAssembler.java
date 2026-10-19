package com.launcher.preparer.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class ManagedManifestAssetAssembler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    public byte[] assemble(
            byte[] templateBytes,
            AssetsIndex assetsIndex
    ) {
        Objects.requireNonNull(templateBytes, "templateBytes");
        Objects.requireNonNull(assetsIndex, "assetsIndex");

        AssetIndexMetadata index = assetsIndex.index();

        if (index == null) {
            throw new IllegalArgumentException(
                    "Asset index metadata is required"
            );
        }

        ObjectNode manifest = readTemplate(templateBytes);

        manifest.set("assets", mapAssets(assetsIndex.assets()));
        manifest.set("assetIndex", mapAssetIndex(index));

        try {
            return MAPPER.writeValueAsBytes(manifest);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize assembled manifest",
                    e
            );
        }
    }

    private ObjectNode readTemplate(byte[] templateBytes) {
        final JsonNode root;

        try {
            root = MAPPER.readTree(templateBytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Invalid manifest template JSON",
                    e
            );
        }

        if (!(root instanceof ObjectNode manifest)) {
            throw new IllegalArgumentException(
                    "Manifest template must be a JSON object"
            );
        }

        return manifest;
    }

    private ArrayNode mapAssets(List<AssetEntry> assets) {
        ArrayNode result = MAPPER.createArrayNode();

        for (AssetEntry asset : assets) {
            ObjectNode entry = result.addObject();

            entry.put("path", asset.path());
            entry.put("sha256", asset.sha256());
            entry.put("size", asset.size());
            entry.put("url", asset.url());
        }

        return result;
    }

    private ObjectNode mapAssetIndex(AssetIndexMetadata index) {
        ObjectNode result = MAPPER.createObjectNode();

        result.put("id", index.id());
        result.put("path", index.path());
        result.put("sha256", index.sha256());
        result.put("size", index.size());
        result.put("url", index.url());

        return result;
    }
}
