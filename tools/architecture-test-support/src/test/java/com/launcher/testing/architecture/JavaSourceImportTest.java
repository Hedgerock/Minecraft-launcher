package com.launcher.testing.architecture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaSourceImportTest {

    @ParameterizedTest
    @CsvSource({
            "java.util.List, false, import java.util.List;",
            "java.util.Objects.requireNonNull, true, import static java.util.Objects.requireNonNull;",
            "java.util.*, false, import java.util.*;",
            "java.util.Objects.*, true, import static java.util.Objects.*;"
    })
    void should_preserve_import_description_and_build_declaration(
            String qualifiedName,
            boolean isStatic,
            String expectedDeclaration
    ) {
        //given & when
        JavaSourceImport result =
                new JavaSourceImport(qualifiedName, isStatic);

        //then
        assertEquals(qualifiedName, result.qualifiedName());
        assertEquals(isStatic, result.isStatic());
        assertEquals(expectedDeclaration, result.declaration());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\t"})
    void should_reject_blank_qualified_name(String qualifiedName) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaSourceImport(qualifiedName, false)
        );

        assertEquals(
                "qualifiedName must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_qualified_name() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaSourceImport(null, false)
        );

        assertEquals("qualifiedName", exception.getMessage());
    }
}
