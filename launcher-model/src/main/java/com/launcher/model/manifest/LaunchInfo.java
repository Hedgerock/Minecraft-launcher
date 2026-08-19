package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;

public record LaunchInfo(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs,
        List<String> classpath
) {

    public LaunchInfo {
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(jvmArgs, "jvmArgs");
        Objects.requireNonNull(gameArgs, "gameArgs");
        Objects.requireNonNull(classpath, "classpath");

        if (mainClass.isBlank()) {
            throw new IllegalArgumentException("mainClass must not be blank");
        }

        if (classpath.isEmpty()) {
            throw new IllegalArgumentException("classpath must not be empty");
        }

        jvmArgs = List.copyOf(jvmArgs);
        gameArgs = List.copyOf(gameArgs);
        classpath = List.copyOf(classpath);
    }

}
