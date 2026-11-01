package com.launcher.testing.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeJavaExecutableFixtureTest {
    private static final long EXECUTION_TIMEOUT_SECONDS = 30;
    private static final long CLEANUP_TIMEOUT_SECONDS = 10;

    @TempDir
    Path tempDir;

    @Test
    void should_prepare_executable_without_starting_process() throws IOException {
        //given
        Path fixtureDirectory = tempDir.resolve("fixture directory");
        Path marker = tempDir.resolve("started.marker");

        //when
        Path executable = FakeJavaExecutableFixture.create(
                fixtureDirectory,
                marker
        );

        //then
        assertEquals(
                fixtureDirectory.toAbsolutePath().normalize(),
                executable.getParent()
        );

        assertTrue(Files.isRegularFile(executable));
        assertTrue(Files.isExecutable(executable));
        assertFalse(Files.exists(marker));
    }

    @Test
    void should_report_version_on_stderr_without_creating_marker() throws Exception {
        //given
        Path marker = tempDir.resolve("started.marker");

        Path executable = FakeJavaExecutableFixture.create(
                tempDir.resolve("fixture directory"),
                marker
        );

        Path workingDirectory = Files.createDirectory(
                tempDir.resolve("game")
        );

        //when
        RunResult result = run(
                executable,
                workingDirectory,
                "-version"
        );

        //then
        assertEquals(0, result.exitCode());
        assertEquals("", result.stdout().strip());
        assertEquals(
                "openjdk version \"21.0.8\" 2025-07-15",
                result.stderr().strip()
        );

        assertFalse(Files.exists(marker));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "started.marker",
            "marker with spaces.marker",
            "marker'quote.marker",
            "marker%percent!.marker",
            "marker&symbols(marker).marker"
    })
    void should_create_marker_when_started_normally(
            String markerName
    ) throws Exception {
        //given
        Path marker = tempDir.resolve(markerName);

        Path executable = FakeJavaExecutableFixture.create(
                tempDir.resolve("fixture directory"),
                marker
        );

        Path workingDirectory = Files.createDirectory(tempDir.resolve("game"));

        //when
        RunResult result = run(
                executable,
                workingDirectory,
                "example.Main"
        );

        //then
        assertEquals(0, result.exitCode());
        assertTrue(Files.isRegularFile(marker));
        assertEquals("started", Files.readString(marker).strip());
        assertEquals("", result.stdout().strip());
        assertEquals("", result.stderr().strip());
    }

    @Test
    void should_reject_null_fixture_directory() {
        //given
        Path marker = tempDir.resolve("started.marker");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> FakeJavaExecutableFixture.create(null, marker)
        );

        assertEquals("fixtureDirectory", exception.getMessage());
    }

    @Test
    void should_reject_null_started_marker() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> FakeJavaExecutableFixture.create(tempDir, null)
        );

        assertEquals("startedMarker", exception.getMessage());
    }

    @Test
    void should_fail_when_fixture_directory_is_a_file() throws IOException {
        //given
        Path fixtureDirectory = Files.writeString(
                tempDir.resolve("not-a-directory"),
                "existing content"
        );

        Path marker = tempDir.resolve("started.marker");

        //when & then
        assertThrows(
                IOException.class,
                () -> FakeJavaExecutableFixture.create(
                        fixtureDirectory,
                        marker
                )
        );

        assertEquals(
                "existing content",
                Files.readString(fixtureDirectory)
        );
    }

    @Test
    void should_fail_when_marker_path_is_a_directory() throws IOException {
        //given
        Path marker = Files.createDirectory(
                tempDir.resolve("marker-directory")
        );

        //when & then
        assertThrows(
                IOException.class,
                () -> FakeJavaExecutableFixture.create(
                        tempDir.resolve("fixture"),
                        marker
                )
        );
    }

    @Test
    void should_reject_marker_matching_executable_path() throws IOException {
        //given
        Path fixtureDirectory = tempDir.resolve("fixture");

        Path executable = FakeJavaExecutableFixture.create(
                fixtureDirectory,
                tempDir.resolve("started.marker")
        );

        String originalScript = Files.readString(executable);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> FakeJavaExecutableFixture.create(
                        fixtureDirectory,
                        executable
                )
        );

        assertEquals(originalScript, Files.readString(executable));
    }

    private RunResult run(
            Path executable,
            Path workingDirectory,
            String... arguments
    ) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(Arrays.asList(arguments));

        Path stdout = Files.createTempFile(
                tempDir,
                "process-stdout-",
                ".txt"
        );

        Path stderr = Files.createTempFile(
                tempDir,
                "process-stderr-",
                ".txt"
        );

        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile());

        try (ProcessScope scope = new ProcessScope(builder.start())) {
            Process process = scope.process();
            process.getOutputStream().close();

            assertTrue(
                    process.waitFor(
                            EXECUTION_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    ),
                    "Fixture process did not finish within the timeout"
            );

            return new RunResult(
                    process.exitValue(),
                    Files.readString(stdout, StandardCharsets.UTF_8),
                    Files.readString(stderr, StandardCharsets.UTF_8)
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        }
    }

    private record RunResult(
            int exitCode,
            String stdout,
            String stderr
    ) {
    }

    private record ProcessScope(Process process) implements AutoCloseable {

        @Override
        public void close() throws Exception {
            boolean restoreInterrupt = Thread.interrupted();

            try (
                    InputStream stdout = process.getInputStream();
                    InputStream stderr = process.getErrorStream();
                    OutputStream stdin = process.getOutputStream()
            ) {
                List<ProcessHandle> handles = new ArrayList<>();

                try (Stream<ProcessHandle> descendants = process.descendants()) {
                    descendants.forEach(handles::add);
                }

                handles.add(process.toHandle());

                for (ProcessHandle handle : handles) {
                    if (handle.isAlive()) {
                        handle.destroyForcibly();
                    }
                }

                CompletableFuture<?>[] exits = handles.stream()
                        .map(ProcessHandle::onExit)
                        .toArray(CompletableFuture<?>[]::new);

                CompletableFuture.allOf(exits).get(
                        CLEANUP_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                );
            } catch (InterruptedException exception) {
                restoreInterrupt = true;
                throw exception;
            } finally {
                if (restoreInterrupt) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
