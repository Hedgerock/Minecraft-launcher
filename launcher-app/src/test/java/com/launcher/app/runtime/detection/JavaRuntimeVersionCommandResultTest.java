package com.launcher.app.runtime.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaRuntimeVersionCommandResultTest {

    @Test
    void should_reject_successful_result_diagnostic_conversion() {
        //given
        JavaRuntimeVersionCommandResult commandResult =
                new JavaRuntimeVersionCommandResult(
                        0,
                        "openjdk version \"21.0.8\" 2025-07-15"
                );

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                commandResult::toDiagnostic
        );

        assertEquals(
                "Successful Java process result cannot be converted to diagnostic",
                exception.getMessage()
        );
    }

    @Test
    void should_create_diagnostic_when_exit_code_is_not_zero() {
        //given
        JavaRuntimeVersionCommandResult commandResult =
                new JavaRuntimeVersionCommandResult(
                        1,
                        "openjdk version \"21.0.8\" 2025-07-15"
                );

        //when
        JavaProcessDiagnostic result = commandResult.toDiagnostic();

        //then
        JavaProcessDiagnostic expectedDiagnostic = new JavaProcessDiagnostic(
                JavaProcessFailureReason.NON_ZERO_EXIT_CODE,
                "Java process exited with code " + commandResult.exitCode()
        );

        assertEquals(
                expectedDiagnostic,
                result
        );
    }

    @Test
    void should_return_false_when_exit_code_is_not_zero() {
        //given & when
        JavaRuntimeVersionCommandResult result =
                new JavaRuntimeVersionCommandResult(
                        1,
                        "openjdk version \"21.0.8\" 2025-07-15"
                );

        //then
        assertFalse(result.isSuccessful());
    }

    @Test
    void should_return_true_when_exit_code_is_zero() {
        //given & when
        JavaRuntimeVersionCommandResult result =
                new JavaRuntimeVersionCommandResult(
                        0,
                        "openjdk version \"21.0.8\" 2025-07-15"
                );

        //then
        assertTrue(result.isSuccessful());
    }
}
