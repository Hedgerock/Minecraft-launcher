package com.launcher.preparer.model;

import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.net.URI;
import java.util.Objects;

public record PreparedVanillaAssetObject(
        VerifiedVanillaAssetObject verified,
        URI uri
) {

    public PreparedVanillaAssetObject {
        Objects.requireNonNull(verified, "verified");
        VanillaAssetResourceUriValidator.requireValid(uri);
    }
}
