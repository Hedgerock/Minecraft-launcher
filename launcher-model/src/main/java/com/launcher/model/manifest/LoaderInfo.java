package com.launcher.model.manifest;

import java.util.Objects;

public record LoaderInfo(
        String type,
        String version
) {

    public LoaderInfo {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(version, "version");

        validateStringForBlank(type, "type");
        validateStringForBlank(version, "version");

    }

    private void validateStringForBlank(String value, String fieldName) {

        if (value.isBlank()) {
            String message = "%s must not be blank".formatted(fieldName);

            throw new IllegalArgumentException(message);
        }

    }

}
