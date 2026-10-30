package com.launcher.testing.architecture;

import java.util.Objects;

public record JavaSourceImport(
        String qualifiedName,
        boolean isStatic
) {

    public JavaSourceImport {
        Objects.requireNonNull(qualifiedName, "qualifiedName");

        if (qualifiedName.isBlank()) {
            throw new IllegalArgumentException(
                    "qualifiedName must not be blank"
            );
        }
    }

    public String declaration() {
        String prefix = isStatic ? "import static " : "import ";

        return prefix + qualifiedName + ";";
    }
}
