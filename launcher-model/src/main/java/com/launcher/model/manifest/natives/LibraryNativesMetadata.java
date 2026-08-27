package com.launcher.model.manifest.natives;

import com.launcher.model.runtime.OperatingSystem;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record LibraryNativesMetadata(
        Map<OperatingSystem, String> classifiers
) {

    public LibraryNativesMetadata {
        Objects.requireNonNull(classifiers, "classifiers");

        classifiers.forEach((os, classifierName) -> {
            Objects.requireNonNull(os, "operatingSystem");
            Objects.requireNonNull(classifierName, "classifierName");

            if (classifierName.isBlank()) {
                throw new IllegalArgumentException("classifierName must not be blank");
            }
        });

        classifiers = Map.copyOf(classifiers);
    }

    public boolean isEmpty() {
        return classifiers.isEmpty();
    }

    public Optional<String> classifierFor(OperatingSystem operatingSystem) {
        Objects.requireNonNull(operatingSystem, "operatingSystem");

        return Optional.ofNullable(classifiers.get(operatingSystem));
    }

}
