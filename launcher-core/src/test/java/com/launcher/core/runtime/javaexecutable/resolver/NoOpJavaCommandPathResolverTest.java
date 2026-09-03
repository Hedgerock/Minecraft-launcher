package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoOpJavaCommandPathResolverTest {
    private final NoOpJavaCommandPathResolver resolver = new NoOpJavaCommandPathResolver();

    @Test
    void should_return_same_java_executable_reference() {
        //given
        String javaExecutable = "java";
        JavaExecutableReference javaExecutableReference = JavaExecutableReference.commandName(javaExecutable);

        //when
        JavaExecutableReference result = resolver.resolve(javaExecutableReference);

        //then
        assertSame(javaExecutableReference, result);
    }

    @Test
    void should_reject_null_java_executable_reference() {
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

}
