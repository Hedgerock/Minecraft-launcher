package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJavaExecutableReferenceResolverTest {
    private final ManifestJavaExecutableReferenceResolver resolver = new ManifestJavaExecutableReferenceResolver();

    @Test
    void should_reject_blank_java_executable() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(" ")
        );

        assertEquals(
                "value must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void should_resolve_java_executable_as_command_name_reference() {
        //given
        String javaExecutable = "java";

        //when
        JavaExecutableReference reference = resolver.resolve(javaExecutable);

        //then
        assertTrue(reference.isCommandName());
        assertEquals(javaExecutable, reference.value());
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
