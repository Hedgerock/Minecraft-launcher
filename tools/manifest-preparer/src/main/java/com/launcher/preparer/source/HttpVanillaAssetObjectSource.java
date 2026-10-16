package com.launcher.preparer.source;

import com.launcher.preparer.VanillaAssetObjectSource;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public final class HttpVanillaAssetObjectSource implements VanillaAssetObjectSource {

    private final URI baseUri;
    private final HttpVanillaAssetResourceSource resourceSource;

    public HttpVanillaAssetObjectSource(
            URI baseUri,
            HttpVanillaAssetResourceSource resourceSource
    ) {
        this.baseUri = requiredBaseUri(baseUri);
        this.resourceSource = Objects.requireNonNull(resourceSource, "resourceSource");
    }

    @Override
    public OpenedVanillaAssetResource open(VanillaAssetObject object) throws IOException {
        Objects.requireNonNull(object, "object");

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
