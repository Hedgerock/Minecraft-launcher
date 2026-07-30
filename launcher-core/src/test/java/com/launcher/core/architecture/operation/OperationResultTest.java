package com.launcher.core.architecture.operation;

import com.launcher.core.operation.result.OperationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationResultTest {

    @Test
    void should_create_success_result() {
        //given when
        OperationResult result = OperationResult.success();

        //then
        assertTrue(result.isSuccess());
        assertTrue(result.errorMessage().isEmpty());
    }

    @Test
    void should_create_failure_result_with_error_message() {
        //given
        String errorMessage = "manifest load failed";
        //given
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
