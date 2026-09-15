package com.launcher.core.architecture.operation;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.result.OperationResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationResultTest {

    @Test
    void should_reject_null_operation_failure() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> OperationResult.failure((OperationFailure) null)
        );

        assertEquals(
                "failure",
                exception.getMessage()
        );
    }

    @Test
    void should_not_have_failure_context_when_result_is_success() {
        //when
        OperationResult result = OperationResult.success();

        //then
        assertTrue(result.isSuccess());
        assertTrue(result.failure().isEmpty());
        assertTrue(result.errorMessage().isEmpty());
    }

    @Test
    void should_create_failure_result_from_operation_failure() {
        //given
        OperationFailure failure = new OperationFailure(
                OperationFailureCode.UNKNOWN,
                "Failure happened",
                Map.of("source", "test")
        );

        //when
        OperationResult result = OperationResult.failure(failure);

        //then
        assertFalse(result.isSuccess());
        assertEquals(failure, result.failure().orElseThrow());
        assertEquals(failure.message(), result.errorMessage().orElseThrow());
    }

    @Test
    void should_create_failure_result_from_message_with_unknown_code() {
        //given
        String message = "Failure happened";

        //when
        OperationResult result = OperationResult.failure(message);

        //then
        assertFalse(result.isSuccess());
        assertEquals(message, result.errorMessage().orElseThrow());
        assertEquals(OperationFailureCode.UNKNOWN, result.failure().orElseThrow().code());
        assertEquals(message, result.failure().orElseThrow().message());
        assertEquals(Map.of(), result.failure().orElseThrow().details());
    }

    @Test
    void should_create_success_result() {
        //given & when
        OperationResult result = OperationResult.success();

        //then
        assertTrue(result.isSuccess());
        assertTrue(result.errorMessage().isEmpty());
    }

    @Test
    void should_create_failure_result_with_error_message() {
        //given
        String errorMessage = "manifest load failed";

        //when
        OperationResult result = OperationResult.failure(errorMessage);

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().isPresent());
        assertEquals(
                errorMessage,
                result.errorMessage().orElseThrow()
        );
    }
}
