package com.launcher.model.runtime;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaExecutableReferenceTest {

    @Test
    void should_return_path_when_java_executable_reference_is_explicit_path_type() {
        //given & when
        JavaExecutableReference reference =
                new JavaExecutableReference(JavaExecutableReferenceType.EXPLICIT_PATH, "/path/to/java");

        //then
        assertEquals(Path.of("/path/to/java"), reference.path());
    }

    @Test
    void should_reject_path_when_java_executable_reference_is_not_explicit_path_type() {
        //given
        JavaExecutableReference reference =
                new JavaExecutableReference(JavaExecutableReferenceType.COMMAND_NAME, "java");

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                reference::path
        );

        assertFalse(reference.isExplicitPath());
        assertTrue(exception.getMessage()
                .contains("Java executable reference is not an explicit path")
        );
    }

    @Test
    void should_create_java_executable_reference_explicit_path() {
        //given & when
        JavaExecutableReference reference = JavaExecutableReference.explicitPath("/path/to/java");

        //then
        assertTrue(reference.isExplicitPath());
        assertEquals("/path/to/java", reference.value());
    }

    @Test
    void should_create_java_executable_reference_command_name() {
        //given & when
        JavaExecutableReference reference = JavaExecutableReference.commandName("java");

        //then
        assertTrue(reference.isCommandName());
        assertEquals("java", reference.value());
    }

    @Test
    void should_reject_blank_value() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaExecutableReference(JavaExecutableReferenceType.EXPLICIT_PATH, " ")
        );

        assertTrue(exception.getMessage().contains("value must not be blank"));
    }

    @Test
    void should_reject_null_value() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaExecutableReference(JavaExecutableReferenceType.EXPLICIT_PATH, null)
        );

        assertTrue(exception.getMessage().contains("value"));
    }

    @Test
    void should_reject_null_type() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaExecutableReference(null, "value")
        );

        assertTrue(exception.getMessage().contains("type"));
    }

}
