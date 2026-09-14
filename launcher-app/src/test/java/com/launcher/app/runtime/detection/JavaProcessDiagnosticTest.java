package com.launcher.app.runtime.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaProcessDiagnosticTest {

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
