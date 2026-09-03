package com.launcher.model.runtime;

import java.nio.file.Path;
import java.util.Objects;

public record JavaExecutableReference(
        JavaExecutableReferenceType type,
        String value
) {

    public JavaExecutableReference {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static JavaExecutableReference commandName(String value) {
        return new JavaExecutableReference(
                JavaExecutableReferenceType.COMMAND_NAME,
                value
        );
    }

    public static JavaExecutableReference explicitPath(String value) {
        return new JavaExecutableReference(
                JavaExecutableReferenceType.EXPLICIT_PATH,
                value
        );
    }

    public boolean isCommandName() {
        return type == JavaExecutableReferenceType.COMMAND_NAME;
    }

    public boolean isExplicitPath() {
        return type == JavaExecutableReferenceType.EXPLICIT_PATH;
    }

    public Path path() {
        if (!isExplicitPath()) {
            throw new IllegalStateException("Java executable reference is not an explicit path");
        }

        return Path.of(value);
    }

}
