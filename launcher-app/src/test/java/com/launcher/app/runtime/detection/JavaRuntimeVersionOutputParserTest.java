package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaRuntimeVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeVersionOutputParserTest {
    private final JavaRuntimeVersionOutputParser parser = new JavaRuntimeVersionOutputParser();

    @Test
    void should_parse_java_runtime_version_from_output_without_minor_or_patch() {
        //given
        String output = "openjdk version \"17\"";

        //when
        JavaRuntimeVersion result = parser.parse(output);

        //then
        assertEquals(
                new JavaRuntimeVersion(17),
                result
        );
    }

    @Test
    void should_reject_malformed_java_runtime_version() {
        //given
        String output = "openjdk version \"not-a-version\"";

        //when & then
        JavaRuntimeVersionParsingException exception = assertThrows(
                JavaRuntimeVersionParsingException.class,
                () -> parser.parse(output)
        );

        assertEquals(
                "Java version cannot be parsed",
                exception.getMessage()
        );
    }

    @Test
    void should_parse_java_runtime_version_from_multiline_output() {
        //given
        String output = """
                openjdk version "17.0.10" 2024-01-16
                OpenJDK Runtime Environment Temurin-17.0.10+7
                OpenJDK 64-Bit Server VM Temurin-17.0.10+7
                """;

        //when
        JavaRuntimeVersion result = parser.parse(output);

        //then
        assertEquals(
                new JavaRuntimeVersion(17),
                result
        );
    }

    @Test
    void should_reject_null_output() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> parser.parse(null)
        );

        assertEquals(
                "output",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_output_without_java_version() {
        //given
        String output = "unexpected output";

        //when & then
        JavaRuntimeVersionParsingException exception = assertThrows(
                JavaRuntimeVersionParsingException.class,
                () -> parser.parse(output)
        );

        assertEquals(
                "Java version cannot be parsed",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_blank_java_runtime_version_output() {
        //when & then
        JavaRuntimeVersionParsingException exception = assertThrows(
                JavaRuntimeVersionParsingException.class,
                () -> parser.parse(" ")
        );

        assertEquals(
                "Output cannot be blank",
                exception.getMessage()
        );
    }

    @Test
    void should_parse_legacy_java_8_runtime_version() {
        //given
        String output = "java version \"1.8.0_421\"";

        //when
        JavaRuntimeVersion result = parser.parse(output);

        //then
        assertEquals(
                new JavaRuntimeVersion(8),
                result
        );
    }

    @Test
    void should_parse_modern_java_runtime_version() {
        //given
        String output = "openjdk version \"21.0.8\" 2025-07-15";

        //when
        JavaRuntimeVersion result = parser.parse(output);

        //then
        assertEquals(
                new JavaRuntimeVersion(21),
                result
        );
    }
}
