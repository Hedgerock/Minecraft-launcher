package com.launcher.preparer.model;

import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.net.URI;
import java.util.Objects;

public record PreparedVanillaAssetIndex(
        VerifiedVanillaAssetIndex verified,
        URI uri
) {

    public PreparedVanillaAssetIndex {
        Objects.requireNonNull(verified, "verified");
        VanillaAssetResourceUriValidator.requireValid(uri);
    }
}
