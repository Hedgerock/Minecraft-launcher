package com.launcher.app.runtime.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaProcessDiagnosticTest {

    @Test
    void should_create_process_cleanup_failed_diagnostic() {
        //given & when
        JavaProcessDiagnostic result =
                JavaProcessDiagnostic.processCleanupFailed();

        //then
        assertEquals(
                JavaProcessFailureReason.PROCESS_CLEANUP_FAILED,
                result.reason()
        );
        assertEquals(
                "Java process cleanup did not complete",
                result.message()
        );
    }

    @Test
    void should_create_output_read_failed_diagnostic() {
        //given & when
        JavaProcessDiagnostic result =
                JavaProcessDiagnostic.outputReadFailed();

        //then
        assertEquals(
                JavaProcessFailureReason.OUTPUT_READ_FAILED,
                result.reason()
        );
        assertEquals(
                "Java process output could not be read",
                result.message()
        );
    }

    @Test
    void should_create_output_limit_exceeded_diagnostic() {
        //given & when
        JavaProcessDiagnostic result =
                JavaProcessDiagnostic.outputLimitExceeded();

        //then
        assertEquals(
                JavaProcessFailureReason.OUTPUT_LIMIT_EXCEEDED,
                result.reason()
        );
        assertEquals(
                "Java process output exceeded the byte limit",
                result.message()
        );
    }

    @Test
    void should_create_process_timed_out_diagnostic() {
        //given & when
        JavaProcessDiagnostic result =
                JavaProcessDiagnostic.processTimedOut();

        //then
        assertEquals(
                JavaProcessFailureReason.PROCESS_TIMED_OUT,
                result.reason()
        );
        assertEquals("Java process timed out", result.message());
    }

    @Test
    void should_create_process_interrupted_diagnostic() {
        //given & when
        JavaProcessDiagnostic result = JavaProcessDiagnostic.processInterrupted();

        //then
        assertEquals(
                JavaProcessFailureReason.PROCESS_INTERRUPTED,
                result.reason()
        );

        assertEquals(
                "Java process was interrupted",
                result.message()
        );
    }

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
