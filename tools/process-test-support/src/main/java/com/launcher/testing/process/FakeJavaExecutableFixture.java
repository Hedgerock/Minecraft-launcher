package com.launcher.testing.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class FakeJavaExecutableFixture {

    private static final String VERSION_OUTPUT =
            "openjdk version \"21.0.8\" 2025-07-15";

    private FakeJavaExecutableFixture() {
    }

    public static Path create(
            Path fixtureDirectory,
            Path startedMarker
    ) throws IOException {
        Objects.requireNonNull(fixtureDirectory, "fixtureDirectory");
        Objects.requireNonNull(startedMarker, "startedMarker");

        Path directory = fixtureDirectory.toAbsolutePath().normalize();
        Path marker = startedMarker.toAbsolutePath().normalize();

        boolean windows = isWindows();

        Path executable = directory.resolve(
                windows ? "fake-java.cmd" : "fake-java.sh"
        );

        if (marker.getParent() == null) {
            throw new IllegalArgumentException(
                    "startedMarker must identify a file"
            );
        }

        if (marker.equals(executable)) {
            throw new IllegalArgumentException(
                    "startedMarker must differ from executable"
            );
        }

        Files.createDirectories(directory);
        Files.createDirectories(marker.getParent());

        if (Files.isDirectory(marker)) {
            throw new IOException(
                    "Marker path is a directory: " + marker
            );
        }

        String script = windows ? windowsScript(marker) : unixScript(marker);

        Files.writeString(
                executable,
                script,
                StandardCharsets.UTF_8
        );

        if (!windows && !executable.toFile().setExecutable(true, true)) {
            throw new IOException(
                    "Could not make fixture executable: " + executable
            );
        }

        if (!Files.isRegularFile(executable) || !Files.isExecutable(executable)) {
            throw new IOException(
                    "Prepared fixture is not executable: " + executable
            );
        }

        return executable;
    }

    private static String windowsScript(Path marker) {
        String markerPath = marker.toString().replace("%", "%%");

        return """
                @echo off
                setlocal EnableExtensions DisableDelayedExpansion
                if "%%~1"=="-version" (
                    echo %s 1>&2
                    exit /b 0
                )
                cd /d "%%TEMP%%" || exit /b 1
                > "%s" echo started
                if errorlevel 1 exit /b 1
                exit /b 0
                """
                .formatted(VERSION_OUTPUT, markerPath)
                .replace("\n", "\r\n");
    }

    private static String unixScript(Path marker) {
        return """
                #!/usr/bin/env sh
                if [ "$1" = "-version" ]; then
                printf '%%s\\n' '%s' >&2
                exit 0
                fi
                cd /tmp || exit 1
                printf '%%s\\n' 'started' > %s || exit 1
                exit 0
                """.formatted(VERSION_OUTPUT, quoteForShell(marker.toString()));
    }

    private static String quoteForShell(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .startsWith("windows");
    }
}
