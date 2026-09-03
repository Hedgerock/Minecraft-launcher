package com.launcher.core.runtime.javaexecutable.resolver.provider;

import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;
import org.junit.jupiter.api.Test;

import java.io.File;
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

    @Test
    void should_return_empty_extensions_when_pathext_is_null() {
        //given
        SystemJavaCommandPathEnvironmentProvider provider = new SystemJavaCommandPathEnvironmentProvider(
                variable -> switch (variable) {
                    case "PATH" -> getSeparatedString("java-bin", "system-bin");
                    case "PATHEXT" -> null;
                    default -> "not-found";
                }
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
                }
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
                }
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
                }
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
                }
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
                }
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
                () -> new SystemJavaCommandPathEnvironmentProvider(null)
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
                }
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
                }
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
