package com.launcher.app.runtime.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaProcessDiagnosticTest {

    @Test
    void should_create_process_start_failed_diagnostic() {
        //given & when
        JavaProcessDiagnostic result = JavaProcessDiagnostic.processStartFailed();

        //then
        assertEquals(
                JavaProcessFailureReason.PROCESS_START_FAILED,
                result.reason()
        );

        assertEquals(
                "Java process could not be started",
                result.message()
        );
    }

    @Test
    void should_create_output_parsing_failed_diagnostic() {
        //given & when
        JavaProcessDiagnostic result = JavaProcessDiagnostic.outputParsingFailed();

        //then
        assertEquals(
                JavaProcessFailureReason.OUTPUT_PARSING_FAILED,
                result.reason()
        );

        assertEquals(
                "Java process output cannot be parsed as Java runtime version",
                result.message()
        );
    }

    @Test
    void should_create_empty_output_diagnostic() {
        //given & when
        JavaProcessDiagnostic result = JavaProcessDiagnostic.emptyOutput();

        //then
        assertEquals(
                JavaProcessFailureReason.EMPTY_OUTPUT,
                result.reason()
        );

        assertEquals(
                "Java process returned empty output",
                result.message()
        );
    }

    @Test
    void should_create_java_process_diagnostic() {
        //given
        JavaProcessFailureReason reason = JavaProcessFailureReason.PROCESS_START_FAILED;
        String message = "Diagnostic complete";

        //when
        JavaProcessDiagnostic result = new JavaProcessDiagnostic(reason, message);

        //then
        assertEquals(
                reason,
                result.reason()
        );
        assertEquals(
                message,
                result.message()
        );
    }

    @Test
    void should_reject_blank_message() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JavaProcessDiagnostic(JavaProcessFailureReason.EMPTY_OUTPUT, " ")
        );

        assertEquals(
                "message must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_message() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaProcessDiagnostic(
                        JavaProcessFailureReason.EMPTY_OUTPUT,
                        null
                )
        );

        assertEquals(
                "message",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_reason() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaProcessDiagnostic(null, "test message")
        );

        assertEquals(
                "reason",
                exception.getMessage()
        );
    }
}
