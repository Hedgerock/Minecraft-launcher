package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;

public record LaunchInfo(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs
) {

    public LaunchInfo {
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(jvmArgs, "jvmArgs");
        Objects.requireNonNull(gameArgs, "gameArgs");

        if (mainClass.isBlank()) {
            throw new IllegalArgumentException("mainClass must not be blank");
        }

        jvmArgs = List.copyOf(jvmArgs);
        gameArgs = List.copyOf(gameArgs);
    }

}
