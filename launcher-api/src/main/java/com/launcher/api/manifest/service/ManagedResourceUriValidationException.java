package com.launcher.api.manifest.service;

public final class ManagedResourceUriValidationException extends RuntimeException {
    private final String resourcePath;

    public ManagedResourceUriValidationException(
            String resourcePath,
            String message
    ) {
        super(message);
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
