package com.launcher.core.architecture.operation.failure;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.failure.OperationFailureMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationFailureMapperTest {
    private final OperationFailureMapper mapper = new OperationFailureMapper();

    @Test
    void should_map_exception_message_to_operation_failure() {
        //given
        IllegalArgumentException exception = new IllegalArgumentException("Failure happened");

        //when
        OperationFailure result = mapper.map(exception);

        //then
        assertEquals(
                "Failure happened",
                result.message()
        );

        assertEquals(OperationFailureCode.UNKNOWN, result.code());

        assertTrue(result.details().isEmpty());
    }

    @Test
    void should_use_exception_class_name_when_message_is_blank() {
        //given
        IllegalArgumentException exception = new IllegalArgumentException(" ");

        //when
        OperationFailure result = mapper.map(exception);

        //then
        assertEquals(
                "IllegalArgumentException",
                result.message()
        );

        assertEquals(OperationFailureCode.UNKNOWN, result.code());

        assertTrue(result.details().isEmpty());
    }

    @Test
    void should_use_exception_class_name_when_message_missing() {
        //given
        IllegalArgumentException exception = new IllegalArgumentException();

        //when
        OperationFailure result = mapper.map(exception);

        //then
        assertEquals(
                "IllegalArgumentException",
                result.message()
        );

        assertEquals(OperationFailureCode.UNKNOWN, result.code());

        assertTrue(result.details().isEmpty());
    }

    @Test
    void should_reject_null_exception() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(null)
        );

        assertEquals("exception", exception.getMessage());
    }
}
