package com.launcher.model.manifest.natives;

import java.util.List;
import java.util.Objects;

public record NativeExtractionRules(
        List<String> excludes
) {

    public NativeExtractionRules {
        Objects.requireNonNull(excludes, "excludes");
        excludes.forEach(exclude -> Objects.requireNonNull(exclude, "exclude"));

        excludes = List.copyOf(excludes);
    }

    public boolean isEmpty() {
        return excludes.isEmpty();
    }

}
