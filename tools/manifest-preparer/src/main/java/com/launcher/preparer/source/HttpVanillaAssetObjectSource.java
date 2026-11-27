package com.launcher.preparer.source;

import com.launcher.preparer.VanillaAssetObjectSource;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public final class HttpVanillaAssetObjectSource implements VanillaAssetObjectSource {

    private static final long DEFAULT_MAX_OBJECT_BYTES = 128L * 1024 * 1024;

    private final URI baseUri;
    private final HttpVanillaAssetResourceSource resourceSource;
    private final long maxObjectBytes;

    public HttpVanillaAssetObjectSource(
            URI baseUri,
            HttpVanillaAssetResourceSource resourceSource
    ) {
        this(baseUri, resourceSource, DEFAULT_MAX_OBJECT_BYTES);
    }

    HttpVanillaAssetObjectSource(
            URI baseUri,
            HttpVanillaAssetResourceSource resourceSource,
            long maxObjectBytes
    ) {
        this.baseUri = requiredBaseUri(baseUri);
        this.resourceSource = Objects.requireNonNull(resourceSource, "resourceSource");

        if (maxObjectBytes <= 0) {
            throw new IllegalArgumentException("maxObjectBytes must be positive");
        }

        this.maxObjectBytes = maxObjectBytes;
    }

    @Override
    public OpenedVanillaAssetResource open(VanillaAssetObject object) throws IOException {
        Objects.requireNonNull(object, "object");

        if (object.size() > maxObjectBytes) {
            throw new IllegalArgumentException("Expected asset object size exceeds maxObjectBytes");
        }

        String hash = object.sha1();
        String relativePath = hash.substring(0, 2) + "/" + hash;
        URI resourceUri = baseUri.resolve(relativePath);

        return resourceSource.open(resourceUri);
    }

    private static URI requiredBaseUri(URI uri) {
        URI validated = VanillaAssetResourceUriValidator.requireValid(uri);
        String path = validated.getRawPath();

        if (
                validated.getRawQuery() != null
                || path == null
                || !path.endsWith("/")
        ) {
            throw new IllegalArgumentException(
                    "Base URI must end with '/' and must not contain a query"
            );
        }

        return validated;
    }
}
