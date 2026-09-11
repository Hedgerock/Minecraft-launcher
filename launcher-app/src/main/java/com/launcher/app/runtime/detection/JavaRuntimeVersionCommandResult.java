package com.launcher.app.runtime.detection;

import java.util.Objects;

record JavaRuntimeVersionCommandResult(
        int exitCode,
        String output
) {

    JavaRuntimeVersionCommandResult {
        Objects.requireNonNull(output, "output");
    }
}
