package com.launcher.api.manifest.service;

import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.ManifestResources;
import com.launcher.model.manifest.ResourceEntry;

import java.net.URI;
import java.util.Objects;

final class ManagedResourceUriValidator {

    void validate(ManifestLoadResult manifestLoadResult) {
        Objects.requireNonNull(manifestLoadResult, "manifestLoadResult");

        Manifest manifest = manifestLoadResult.manifest();
        ManifestResources.from(manifest).forEach(this::validate);
    }

    private void validate(ResourceEntry resourceEntry) {
        URI uri;

        try {
            uri = URI.create(resourceEntry.url());
        } catch (IllegalArgumentException e) {
            throw validationException(
                    resourceEntry,
                    "Resource URI is invalid"
            );
        }

        if (!uri.isAbsolute()) {
            throw validationException(
                    resourceEntry,
                    "Resource URI must be absolute"
            );
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw validationException(
                    resourceEntry,
                    "Resource URI must use HTTPS"
            );
        }

        if (uri.getHost() == null) {
            throw validationException(
                    resourceEntry,
                    "Resource URI must contain a host"
            );
        }

        if (uri.getRawUserInfo() != null) {
            throw validationException(
                    resourceEntry,
                    "Resource URI must not contain user info"
            );
        }

        if (uri.getFragment() != null) {
            throw validationException(
                    resourceEntry,
                    "Resource URI must not contain a fragment"
            );
        }
    }

    private ManagedResourceUriValidationException validationException(
            ResourceEntry resourceEntry,
            String reason
    ) {
        return new ManagedResourceUriValidationException(
                resourceEntry.path(),
                reason
        );
    }
}
