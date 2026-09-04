package com.launcher.core.runtime.javaexecutable.resolver.provider;

import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemJavaCommandPathEnvironmentProviderTest {

    private String getSeparatedString(String... targets) {
        return String.join(
                File.pathSeparator,
                targets
        );
    }

    private Path getValidPath(String target, String value) {
        if (value.equals(target)) {
            throw new InvalidPathException(
                    value,
                    "invalid path"
            );
        }

        return Path.of(value);
    }

    @Test
    void should_reject_null_path_parser() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SystemJavaCommandPathEnvironmentProvider(
                        variable -> "not-null",
                        null
                )
        );

        assertEquals(
                "pathParser",
                exception.getMessage()
        );
    }

    @Test
    void should_ignore_invalid_path_entries() {
        //given
        String validFirst = "first";
        String invalid = "invalid";
        String validSecond = "second";

        String path = validFirst + File.pathSeparator + invalid
                + File.pathSeparator
                + validSecond;

        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> path;
                    case "PATHEXT" -> null;
                    default -> "not-found";
                },
                value -> getValidPath(invalid, value)
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertEquals(
                List.of(Path.of(validFirst), Path.of(validSecond)),
                result.directories()
        );
    }

    @Test
    void should_return_empty_extensions_when_pathext_is_null() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> null;
                    default -> "not-found";
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertTrue(result.executableExtensions().isEmpty());
    }

    @Test
    void should_return_empty_directories_when_path_is_null() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> null;
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> "not-found";
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertTrue(result.directories().isEmpty());
    }

    @Test
    void should_return_empty_directories_when_path_is_blank() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> " ";
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertTrue(result.directories().isEmpty());
    }

    @Test
    void should_return_empty_extensions_when_pathext_is_blank() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> " ";
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertTrue(result.executableExtensions().isEmpty());
    }

    @Test
    void should_ignore_blank_pathext_segments() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertEquals(
                List.of(".exe", ".bat", ".cmd", ".sh"),
                result.executableExtensions()
        );
    }

    @Test
    void should_ignore_blank_path_segments() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", " ", "system-bin", " ");
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertEquals(
                List.of(
                        Path.of("java-bin"),
                        Path.of("system-bin")
                ),
                result.directories()
        );
    }

    @Test
    void should_reject_null_environment_variable_provider() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SystemJavaCommandPathEnvironmentProvider(null, Path::of)
        );

        assertEquals(
                "environmentVariableProvider",
                exception.getMessage()
        );
    }

    @Test
    void should_build_executable_extensions_from_pathext() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertEquals(
                List.of(".exe", ".bat", ".cmd", ".sh"),
                result.executableExtensions()
        );
    }

    @Test
    void should_build_path_directories_from_path_environment() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> getSeparatedString(".EXE", ".BAT", " ", ".CMD", ".SH", " ");
                    default -> null;
                },
                Path::of
        );

        //when
        JavaCommandPathEnvironment result = provider.current();

        //then
        assertEquals(
                List.of(
                        Path.of("java-bin"),
                        Path.of("system-bin")
                ),
                result.directories()
        );
    }

}
