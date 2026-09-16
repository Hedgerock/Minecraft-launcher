package com.launcher.model.manifest;

import com.launcher.model.runtime.JavaVersionRequirement;

import java.util.List;
import java.util.Objects;

public record LaunchInfo(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs,
        List<String> classpath,
        String javaExecutable,
        JavaVersionRequirement javaVersionRequirement,
        List<String> authArgs
) {

    public LaunchInfo(
            String mainClass,
            List<String> jvmArgs,
            List<String> gameArgs,
            List<String> classpath,
            String javaExecutable,
            JavaVersionRequirement javaVersionRequirement
    ) {
        this(
                mainClass,
                jvmArgs,
                gameArgs,
                classpath,
                javaExecutable,
                javaVersionRequirement,
                List.of()
        );
    }

    public LaunchInfo {
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(jvmArgs, "jvmArgs");
        Objects.requireNonNull(gameArgs, "gameArgs");
        Objects.requireNonNull(classpath, "classpath");
        Objects.requireNonNull(javaExecutable, "javaExecutable");
        Objects.requireNonNull(javaVersionRequirement, "javaVersionRequirement");
        Objects.requireNonNull(authArgs, "authArgs");

        validateFieldOnBlankValue(mainClass, "mainClass");
        validateFieldOnBlankValue(javaExecutable, "javaExecutable");

        if (classpath.isEmpty()) {
            throw new IllegalArgumentException("classpath must not be empty");
        }

        jvmArgs = List.copyOf(jvmArgs);
        gameArgs = List.copyOf(gameArgs);
        classpath = List.copyOf(classpath);
        authArgs = List.copyOf(authArgs);
    }

    private void validateFieldOnBlankValue(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

}
