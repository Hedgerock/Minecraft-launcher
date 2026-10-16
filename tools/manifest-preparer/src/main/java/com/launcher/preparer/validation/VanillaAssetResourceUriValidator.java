package com.launcher.preparer.validation;

import java.net.URI;
import java.util.Objects;

public final class VanillaAssetResourceUriValidator {

    private VanillaAssetResourceUriValidator() {
    }

    public static URI requireValid(URI uri) {
        Objects.requireNonNull(uri, "uri");

        if (
                !uri.isAbsolute()
                        || !"https".equalsIgnoreCase(uri.getScheme())
                        || uri.getHost() == null
                        || uri.getRawUserInfo() != null
                        || uri.getRawFragment() != null
        ) {
            throw new IllegalArgumentException(
                    "Resource URI must be an absolute HTTPS URI with a " +
                            "host and without user info or fragment"
            );
        }

        return uri;
    }
}
