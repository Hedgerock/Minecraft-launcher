package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJavaExecutableReferenceResolverTest {
    private final ManifestJavaExecutableReferenceResolver resolver = new ManifestJavaExecutableReferenceResolver();

    @Test
    void should_resolve_java_executable_as_explicit_path() {
        //given
        List<String> explicitPaths = List.of(
                "runtime/java",
                "runtime\\java.exe",
                "./java",
                "../runtime/java",
                "C:\\Java\\bin\\java.exe"
        );

        //when
        List<JavaExecutableReference> results = explicitPaths.stream()
                .map(resolver::resolve)
                .toList();

        //then
        assertTrue(results.stream().allMatch(JavaExecutableReference::isExplicitPath));
        assertEquals(
                List.of(
                        JavaExecutableReference.explicitPath("runtime/java"),
                        JavaExecutableReference.explicitPath("runtime\\java.exe"),
                        JavaExecutableReference.explicitPath("./java"),
                        JavaExecutableReference.explicitPath("../runtime/java"),
                        JavaExecutableReference.explicitPath("C:\\Java\\bin\\java.exe")
                ),
                results
        );
    }

    @Test
    void should_reject_blank_java_executable() {
        List<String> messages = new ArrayList<>();
        List<String> javaExecutables = List.of(" ", "\t", "\n", "");

        //when & then
        javaExecutables.forEach(commandName -> {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> resolver.resolve(commandName)
            );

            messages.add(exception.getMessage());
        });

        assertTrue(
                messages.stream().allMatch(message -> message.contains("javaExecutable must not be blank"))
        );
    }

    @Test
    void should_resolve_java_executable_as_command_name_reference() {
        //given
        List<String> commandNames = List.of("java", "java.exe");

        //when
        List<JavaExecutableReference> results = commandNames.stream()
                .map(resolver::resolve)
                .toList();

        //then
        assertTrue(results.stream().allMatch(JavaExecutableReference::isCommandName));
        assertEquals(
                List.of(
                        JavaExecutableReference.commandName("java"),
                        JavaExecutableReference.commandName("java.exe")
                ),
                results
        );
    }

    @Test
    void should_reject_null_java_executable() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null)
        );

        assertEquals(
                "javaExecutable",
                exception.getMessage()
        );
    }

}
