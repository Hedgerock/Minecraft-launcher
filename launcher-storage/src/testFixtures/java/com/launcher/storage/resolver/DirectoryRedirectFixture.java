package com.launcher.storage.resolver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DirectoryRedirectFixture {

    private DirectoryRedirectFixture() {
    }

    public static void create(Path link, Path target) throws IOException {
        Path absoluteLink = link.toAbsolutePath().normalize();
        Path absoluteTarget = target.toAbsolutePath().normalize();

        if (!Files.isDirectory(absoluteTarget)) {
            throw new IOException("Redirect target is not a directory: " + absoluteTarget);
        }

        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            createWindowsJunction(absoluteLink, absoluteTarget);
        } else {
            Files.createSymbolicLink(absoluteLink, absoluteTarget);
        }

        if (!Files.isSameFile(absoluteLink, absoluteTarget)) {
            throw new IOException("Directory redirect points to an unexpected target");
        }
    }

    private static void createWindowsJunction(Path link, Path target) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                "$ErrorActionPreference = 'Stop'; " +
                        "New-Item -ItemType Junction " +
                        "-Path $env:LAUNCHER_TEST_LINK " +
                        "-Target $env:LAUNCHER_TEST_TARGET | Out-Null"
        );

        processBuilder.environment().put("LAUNCHER_TEST_LINK", link.toString());
        processBuilder.environment().put("LAUNCHER_TEST_TARGET", target.toString());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("Timed out while creating directory junction");
            }
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while creating directory junction", e);
        }

        if (process.exitValue() != 0) {
            String output = new String(
                    process.getInputStream().readAllBytes(),
                    Charset.defaultCharset()
            );

            throw new IOException(
                    "Failed to create directory junction: " + output
            );
        }
    }
}
