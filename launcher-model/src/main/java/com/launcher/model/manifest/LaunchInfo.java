package com.launcher.model.manifest;

import java.util.List;

public record LaunchInfo(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs
) {

    public LaunchInfo {
        jvmArgs = List.copyOf(jvmArgs);
        gameArgs = List.copyOf(gameArgs);
    }

}
