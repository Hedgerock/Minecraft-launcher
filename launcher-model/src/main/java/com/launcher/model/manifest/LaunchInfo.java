package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;

public record LaunchInfo(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs,
        List<String> classpath,
        String javaExecutable
) {

    public LaunchInfo {
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(jvmArgs, "jvmArgs");
        Objects.requireNonNull(gameArgs, "gameArgs");
        Objects.requireNonNull(classpath, "classpath");
        Objects.requireNonNull(javaExecutable, "javaExecutable");

        validateFieldOnBlankValue(mainClass, "mainClass");
        validateFieldOnBlankValue(javaExecutable, "javaExecutable");

        if (classpath.isEmpty()) {
            throw new IllegalArgumentException("classpath must not be empty");
        }

        jvmArgs = List.copyOf(jvmArgs);
        gameArgs = List.copyOf(gameArgs);
        classpath = List.copyOf(classpath);
    }

    private void validateFieldOnBlankValue(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

}
