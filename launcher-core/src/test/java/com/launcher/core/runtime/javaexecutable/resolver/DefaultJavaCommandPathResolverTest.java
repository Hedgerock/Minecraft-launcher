package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.core.runtime.javaexecutable.exception.JavaCommandPathResolutionException;
import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJavaCommandPathResolverTest {
    private static final JavaCommandPathEnvironment EMPTY_ENVIRONMENT = new JavaCommandPathEnvironment(
            List.of(),
            List.of()
    );

    @Test
    void should_reject_null_java_executable_reference() {
        //given
        DefaultJavaCommandPathResolver resolver = new DefaultJavaCommandPathResolver(EMPTY_ENVIRONMENT);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null)
        );

        assertEquals(
                "javaExecutableReference",
                exception.getMessage()
        );
    }

    @Test
    void should_not_resolve_command_name_using_extension_when_candidate_is_directory(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);

        Files.createDirectory(directory.resolve("java.exe"));

        JavaCommandPathEnvironment environment = new JavaCommandPathEnvironment(
                List.of(directory),
                List.of(".exe", ".bat")
        );

        DefaultJavaCommandPathResolver resolver = new DefaultJavaCommandPathResolver(environment);

        //when & then
        assertThrows(
                JavaCommandPathResolutionException.class,
                () -> resolver.resolve(JavaExecutableReference.commandName("java"))
        );
    }

    @Test
    void should_throw_when_command_not_found() {
        //given
        JavaExecutableReference reference = JavaExecutableReference.commandName("nonExistingCommand");
        DefaultJavaCommandPathResolver resolver = new DefaultJavaCommandPathResolver(EMPTY_ENVIRONMENT);

        //when & then
        JavaCommandPathResolutionException exception = assertThrows(
                JavaCommandPathResolutionException.class,
                () -> resolver.resolve(reference)
        );

        assertEquals(
                "Java command not found: nonExistingCommand",
                exception.getMessage()
        );
    }


    @Test
    void should_resolve_command_name_using_executable_extension(@TempDir Path tempDir) throws IOException {
        //given
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);

        Path javaWithExtension = directory.resolve("java.exe");
        Files.createFile(javaWithExtension);

        JavaExecutableReference reference = JavaExecutableReference.commandName("java");
        JavaCommandPathEnvironment environment = new JavaCommandPathEnvironment(
                List.of(directory),
                List.of(".exe", ".bat")
        );

        DefaultJavaCommandPathResolver resolver = new DefaultJavaCommandPathResolver(environment);

        //when
        JavaExecutableReference result = resolver.resolve(reference);

        //then
        assertTrue(result.isExplicitPath());
        assertEquals(javaWithExtension, result.path());
    }

    @Test
    void should_resolve_command_name_from_path_directory(@TempDir Path tempDir) throws IOException {
        //given
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);

        Path java = directory.resolve("java");
        Files.createFile(java);

        JavaExecutableReference reference = JavaExecutableReference.commandName("java");
        JavaCommandPathEnvironment environment = new JavaCommandPathEnvironment(
                List.of(directory),
                List.of(".exe", ".bat")
        );

        DefaultJavaCommandPathResolver resolver = new DefaultJavaCommandPathResolver(environment);

        //when
        JavaExecutableReference result = resolver.resolve(reference);

        //then
        assertTrue(result.isExplicitPath());
        assertEquals(java, result.path());
    }

    @Test
    void should_return_explicit_path_reference_as_is() {
        //given
        JavaExecutableReference reference =
                JavaExecutableReference.explicitPath("explicitPath");

        DefaultJavaCommandPathResolver resolver =
                new DefaultJavaCommandPathResolver(EMPTY_ENVIRONMENT);

        //when
        JavaExecutableReference result = resolver.resolve(reference);

        //then
        assertTrue(result.isExplicitPath());
        assertSame(reference, result);
    }

    @Test
    void should_reject_null_java_command_path_environment() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultJavaCommandPathResolver(null)
        );

        assertEquals(
                "javaCommandPathEnvironment",
                exception.getMessage()
        );
    }
}
