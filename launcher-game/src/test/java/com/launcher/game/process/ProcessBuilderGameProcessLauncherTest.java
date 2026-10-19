package com.launcher.game.process;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;
import com.launcher.game.support.GameProcessOutputProbe;
import com.launcher.game.support.GameProcessTestApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessBuilderGameProcessLauncherTest {
    private final GameProcessLauncher launcher =
            new ProcessBuilderGameProcessLauncher();

    @Test
    void should_launch_process_with_command(@TempDir Path tempDir) throws Exception {
        //given
        GameLaunchPlan plan = new GameLaunchPlan(
                tempDir,
                List.of(javaExecutable(), "-version")
        );

        //when
        Process process = launcher.launch(plan);

        //then
        try {
            assertSuccessfulExit(process);
        } finally {
            stopProcess(process);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"stdout", "stderr"})
    void should_complete_process_when_output_is_not_read(
            String output,
            @TempDir Path tempDir
    ) throws Exception {
        //given
        GameLaunchPlan plan = fixturePlan(tempDir, output, "unused");

        Path stdOutFile = tempDir.resolve("probe-stdout.log");
        Path stderrFile = tempDir.resolve("probe-stderr.log");

        List<String> command = new ArrayList<>(List.of(
                javaExecutable(),
                "-cp",
                fixtureClasspath(),
                GameProcessOutputProbe.class.getName(),
                tempDir.toAbsolutePath().toString()
        ));

        command.addAll(plan.command());

        //when
        Process probe = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .redirectOutput(stdOutFile.toFile())
                .redirectError(stderrFile.toFile())
                .start();

        //then
        try {
            assertTrue(
                    probe.waitFor(60, TimeUnit.SECONDS),
                    "Output probe did not finish within the timeout"
            );

            try (InputStream errors = Files.newInputStream(stderrFile)) {
                assertEquals(
                        0,
                        probe.exitValue(),
                        new String(
                                errors.readNBytes(4096),
                                StandardCharsets.UTF_8
                        )
                );
            }

            Path outputFile = output.equals("stdout")
                    ? stdOutFile
                    : stderrFile;

            String expectedOutput = ("x".repeat(1023) + "\n").repeat(256);
            String actualOutput = Files.readString(outputFile, StandardCharsets.UTF_8);

            assertTrue(
                    actualOutput.contains(expectedOutput),
                    "Expected fixture payload in inherited " + output
            );
        } finally {
            stopProcess(probe);
        }
    }

    @Test
    void should_use_game_directory_as_working_directory(@TempDir Path tempDir) throws Exception {
        //given
        String markerName = "game-started.marker";
        Path marker = tempDir.resolve(markerName);

        GameLaunchPlan plan = fixturePlan(
                tempDir,
                "marker",
                markerName
        );

        //when
        Process process = launcher.launch(plan);

        //then
        try {
            assertSuccessfulExit(process);
            assertEquals("started", Files.readString(marker));
        } finally {
            stopProcess(process);
        }
    }

    @Test
    void should_throw_game_launch_exception_when_process_cannot_be_started(@TempDir Path tempDir) {
        //given
        GameLaunchPlan plan = new GameLaunchPlan(
                tempDir,
                List.of(
                        "non-existing-executable"
                )
        );

        //when
        GameLaunchException exception = assertThrows(
                GameLaunchException.class,
                () -> launcher.launch(plan)
        );

        //then
        assertNotNull(exception.getCause());
        assertInstanceOf(IOException.class, exception.getCause());

        assertEquals(
                "Failed to launch game process",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_game_launch_plan() {
        //when & then
        assertThrows(
                NullPointerException.class,
                () -> launcher.launch(null)
        );
    }

    private String fixtureClasspath() throws URISyntaxException {
        List<String> entries = new ArrayList<>();

        for (Class<?> type : List.of(
                GameProcessOutputProbe.class,
                ProcessBuilderGameProcessLauncher.class,
                GameLaunchPlan.class
        )) {
            entries.add(
                    Path.of(
                            type.getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI()
                    ).toAbsolutePath().toString()
            );
        }

        return String.join(
                File.pathSeparator,
                entries.stream().distinct().toList()
        );
    }

    private GameLaunchPlan fixturePlan(
            Path gameDirectory,
            String mode,
            String markerName
    ) throws URISyntaxException {
        return new GameLaunchPlan(
                gameDirectory,
                List.of(
                        javaExecutable(),
                        "-cp",
                        fixtureClasspath(),
                        GameProcessTestApplication.class.getName(),
                        mode,
                        markerName
                )
        );
    }

    private void assertSuccessfulExit(Process process) throws InterruptedException {
        assertTrue(
                process.waitFor(30, TimeUnit.SECONDS),
                "Test process did not finish within the timeout"
        );
        assertEquals(0, process.exitValue());
    }

    private void stopProcess(Process process) throws Exception {
        List<ProcessHandle> handles = new ArrayList<>();

        try (Stream<ProcessHandle> descendants = process.descendants()) {
            descendants.forEach(handles::add);
        }

        handles.add(process.toHandle());

        for (ProcessHandle handle : handles) {
            handle.destroyForcibly();
        }

        CompletableFuture<?>[] exits = handles.stream()
                .map(ProcessHandle::onExit)
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(exits).get(10, TimeUnit.SECONDS);
    }

    private String javaExecutable() {
        boolean windows = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win");

        String javaHome = System.getProperty("java.home");
        String executable = windows ? "java.exe" : "java";

        return Path.of(javaHome, "bin", executable).toString();
    }
}
