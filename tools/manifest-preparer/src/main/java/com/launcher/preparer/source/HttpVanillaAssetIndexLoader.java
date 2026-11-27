package com.launcher.preparer.source;

import com.launcher.preparer.VanillaAssetIndexReader;
import com.launcher.preparer.model.PreparedVanillaAssetIndex;
import com.launcher.preparer.model.VerifiedVanillaAssetIndex;
import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public final class HttpVanillaAssetIndexLoader {

    private static final int DEFAULT_MAX_INDEX_BYTES = 16 * 1024 * 1024;

    private final HttpVanillaAssetResourceSource source;
    private final VanillaAssetIndexReader reader;
    private final int maxIndexBytes;

    public HttpVanillaAssetIndexLoader(
            HttpVanillaAssetResourceSource source,
            VanillaAssetIndexReader reader
    ) {
        this(source, reader, DEFAULT_MAX_INDEX_BYTES);
    }

    HttpVanillaAssetIndexLoader(
            HttpVanillaAssetResourceSource source,
            VanillaAssetIndexReader reader,
            int maxIndexBytes
    ) {
        this.source = Objects.requireNonNull(source, "source");
        this.reader = Objects.requireNonNull(reader, "reader");

        if (maxIndexBytes <= 0 || maxIndexBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "maxIndexBytes must be between 1 and Integer.MAX_VALUE - 1"
            );
        }

        this.maxIndexBytes = maxIndexBytes;
    }

    public PreparedVanillaAssetIndex load(
            URI uri,
            String expectedSha1,
            long expectedSize
    ) throws IOException {
        validateFields(uri, expectedSha1, expectedSize);

        try (OpenedVanillaAssetResource resource = source.open(uri)) {
            int readLimit = Math.toIntExact(expectedSize + 1);

            byte[] bytes = resource.getInput().readNBytes(readLimit);

            VerifiedVanillaAssetIndex verified = reader.read(
                    bytes,
                    expectedSha1,
                    expectedSize
            );

            return new PreparedVanillaAssetIndex(
                    verified,
                    resource.getUri()
            );
        }
    }

    private void validateFields(
            URI uri,
            String expectedSha1,
            long expectedSize
    ) {
        VanillaAssetResourceUriValidator.requireValid(uri);
        Objects.requireNonNull(expectedSha1, "expectedSha1");

        String sha1Pattern = "^[0-9a-f]{40}$";

        if (!expectedSha1.matches(sha1Pattern)) {
            throw new IllegalArgumentException(
                    "expectedSha1 must contain 40 lowercase hexadecimal characters"
            );
        }

        if (expectedSize < 0 || expectedSize >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "expectedSize must be between 0 and Integer.MAX_VALUE - 1"
            );
        }

        if (expectedSize > maxIndexBytes) {
            throw new IllegalArgumentException(
                    "Expected asset index size exceeds maxIndexBytes"
            );
        }
    }
}
