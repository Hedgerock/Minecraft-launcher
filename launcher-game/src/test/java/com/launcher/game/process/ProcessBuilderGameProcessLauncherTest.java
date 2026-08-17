package com.launcher.game.process;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessBuilderGameProcessLauncherTest {

    @Test
    void should_launch_process_with_command(@TempDir Path tempDir) throws Exception {
        //given
        GameLaunchPlan plan = new GameLaunchPlan(
                tempDir,
                List.of(
                        javaExecutable(),
                        "-version"
                )
        );

        GameProcessLauncher launcher = new ProcessBuilderGameProcessLauncher();

        //when
        Process process = launcher.launch(plan);

        //then
        assertEquals(0, process.waitFor());
    }

    @Test
    void should_use_game_directory_as_working_directory(@TempDir Path tempDir) throws Exception {
        List<String> command = isWindows()
                ? List.of("cmd.exe", "/c", "cd")
                : List.of("pwd");

        //given
        GameLaunchPlan plan = new GameLaunchPlan(tempDir, command);

        GameProcessLauncher launcher = new ProcessBuilderGameProcessLauncher();

        //when
        Process process = launcher.launch(plan);

        //then
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String actualDirectory = reader.readLine();

            assertEquals(
                    tempDir.toAbsolutePath().toString().trim(),
                    actualDirectory.trim()
            );
        }

        assertEquals(0, process.waitFor());
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

        GameProcessLauncher launcher = new ProcessBuilderGameProcessLauncher();

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

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private String javaExecutable() {
        String javaHome = System.getProperty("java.home");
        String executable = isWindows() ? "java.exe" : "java";

        return Path.of(javaHome, "bin", executable).toString();
    }

}