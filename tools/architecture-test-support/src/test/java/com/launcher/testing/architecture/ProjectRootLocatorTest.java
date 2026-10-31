package com.launcher.testing.architecture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectRootLocatorTest {

    @TempDir
    Path tempDir;

    private final ProjectRootLocator locator = new ProjectRootLocator();

    @ParameterizedTest
    @ValueSource(strings = {
            "settings.gradle",
            "settings.gradle.kts"
    })
    void should_find_root_when_start_directory_contains_settings_file(
            String fileName
    ) throws IOException {
        //given
        Files.createFile(tempDir.resolve(fileName));

        //when
        Path result = locator.findFrom(tempDir);

        //then
        assertEquals(tempDir.toAbsolutePath(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "settings.gradle",
            "settings.gradle.kts"
    })
    void should_find_root_from_nested_directory(
            String fileName
    ) throws IOException {
        //given
        Path projectRoot = Files.createDirectory(tempDir.resolve("project"));

        Files.createFile(projectRoot.resolve(fileName));

        Path startDirectory = Files.createDirectories(projectRoot.resolve("module/src/test/java"));

        //when
        Path result = locator.findFrom(startDirectory);

        //then
        assertEquals(projectRoot.toAbsolutePath(), result);
    }

    @Test
    void should_find_nearest_project_root() throws IOException {
        //given
        Files.createFile(tempDir.resolve("settings.gradle"));

        Path nestedProject = Files.createDirectory(
                tempDir.resolve("nested-project")
        );

        Files.createFile(nestedProject.resolve("settings.gradle.kts"));

        Path startDirectory = Files.createDirectories(
                nestedProject.resolve("src/test/java")
        );

        //when
        Path result = locator.findFrom(startDirectory);

        //then
        assertEquals(nestedProject.toAbsolutePath(), result);
    }

    @Test
    void should_ignore_directory_named_as_settings_file() throws IOException {
        //given
        Files.createFile(tempDir.resolve("settings.gradle"));

        Path startDirectory = Files.createDirectory(
                tempDir.resolve("module")
        );

        Files.createDirectory(
                startDirectory.resolve("settings.gradle.kts")
        );

        //when
        Path result = locator.findFrom(startDirectory);

        //then
        assertEquals(tempDir.toAbsolutePath(), result);
    }

    @Test
    void should_fail_when_project_root_does_not_exist() throws IOException {
        //given
        Path archive = tempDir.resolve("isolated-project.zip");

        try (FileSystem fileSystem = FileSystems.newFileSystem(
                archive,
                Map.of("create", "true")
        )) {
            Path startDirectory = Files.createDirectories(
                    fileSystem.getPath("/module/src")
            );

            //when & then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> locator.findFrom(startDirectory)
            );

            assertEquals("Project root not found", exception.getMessage());
        }
    }

    @Test
    void should_reject_missing_start_directory() {
        //given
        Path startDirectory = tempDir.resolve("missing");

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> locator.findFrom(startDirectory)
        );
    }

    @Test
    void should_reject_start_path_that_is_not_directory() throws IOException {
        //given
        Path startPath = Files.createFile(tempDir.resolve("file.txt"));

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> locator.findFrom(startPath)
        );
    }

    @Test
    void should_reject_null_start_directory() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> locator.findFrom(null)
        );

        assertEquals("startDirectory", exception.getMessage());
    }
}
