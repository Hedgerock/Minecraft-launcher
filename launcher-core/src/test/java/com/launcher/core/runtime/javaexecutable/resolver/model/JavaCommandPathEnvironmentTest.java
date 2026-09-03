package com.launcher.core.runtime.javaexecutable.resolver.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaCommandPathEnvironmentTest {

    @Test
    void should_reject_executable_extensions_mutation_accessor() {
        //given
        List<Path> directories = List.of(Path.of("firstPath"), Path.of("secondPath"));
        List<String> executableExtensions = new ArrayList<>(
                List.of(".exe", ".bat")
        );

        JavaCommandPathEnvironment environment =
                new JavaCommandPathEnvironment(directories, executableExtensions);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> environment.executableExtensions().add("sh")
        );
    }

    @Test
    void should_reject_directories_mutation_accessor() {
        //given
        Path firstPath = Path.of("firstPath");
        Path secondPath = Path.of("secondPath");
        List<Path> directories = new ArrayList<>(List.of(firstPath, secondPath));
        List<String> executableExtensions = List.of(".exe", ".bat");

        JavaCommandPathEnvironment environment =
                new JavaCommandPathEnvironment(directories, executableExtensions);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> environment.directories().add(Path.of("thirdPath"))
        );
    }

    @Test
    void should_create_immutable_executable_extensions() {
        //given
        List<Path> directories = List.of(Path.of("firstPath"), Path.of("secondPath"));
        List<String> executableExtensions = new ArrayList<>(
                List.of(".exe", ".bat")
        );

        JavaCommandPathEnvironment environment =
                new JavaCommandPathEnvironment(directories, executableExtensions);

        //when
        executableExtensions.add("sh");

        //then
        assertEquals(
                List.of(".exe", ".bat"),
                environment.executableExtensions()
        );
    }

    @Test
    void should_create_immutable_directories() {
        //given
        Path firstPath = Path.of("firstPath");
        Path secondPath = Path.of("secondPath");
        List<Path> directories = new ArrayList<>(List.of(firstPath, secondPath));
        List<String> executableExtensions = List.of(".exe", ".bat");

        JavaCommandPathEnvironment environment =
                new JavaCommandPathEnvironment(directories, executableExtensions);

        //when
        directories.add(Path.of("thirdPath"));

        //then
        assertEquals(
                List.of(firstPath, secondPath),
                environment.directories()
        );
    }

    @Test
    void should_reject_null_executable_extensions() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaCommandPathEnvironment(
                        List.of(Path.of("directory")),
                        null
                )
        );

        assertEquals("executableExtensions", exception.getMessage());
    }

    @Test
    void should_reject_null_directories() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaCommandPathEnvironment(
                        null,
                        List.of(".exe", ".bat")
                )
        );

        assertEquals("directories", exception.getMessage());
    }

}
