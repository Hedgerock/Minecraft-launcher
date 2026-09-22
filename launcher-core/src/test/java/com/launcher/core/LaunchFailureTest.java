package com.launcher.core;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchFailureTest {

    @Test
    void should_reject_blank_lifecycle_message() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> LaunchFailure.lifecycle(" ")
        );

        assertEquals("message must not be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_lifecycle_message() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchFailure.lifecycle(null)
        );

        assertEquals("message", exception.getMessage());
    }

    @Test
    void should_reject_null_operation_failure() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchFailure.operation(
                        OperationType.LAUNCH_GAME,
                        null
                )
        );

        assertEquals("operationFailure", exception.getMessage());
    }

    @Test
    void should_reject_null_operation_type() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> LaunchFailure.operation(null, getDefaultOperationFailure())
        );

        assertEquals("operationType", exception.getMessage());
    }

    @Test
    void should_create_lifecycle_launch_failure() {
        //given & when
        LaunchFailure result = LaunchFailure.lifecycle("Lifecycle error");

        //then
        assertTrue(result.operationType().isEmpty());
        assertTrue(result.operationFailure().isEmpty());

        assertEquals(
                "Lifecycle error",
                result.message()
        );
    }

    @Test
    void should_use_operation_failure_message() {
        //given
        OperationType expectedOperationType = OperationType.LAUNCH_GAME;
        OperationFailure expectedFailure = getDefaultOperationFailure();

        //when
        LaunchFailure result = LaunchFailure
                .operation(expectedOperationType, expectedFailure);

        //then
        assertEquals(
                expectedFailure.message(),
                result.message()
        );
    }

    @Test
    void should_create_operation_launch_failure() {
        //given
        OperationType expectedOperationType = OperationType.LAUNCH_GAME;
        OperationFailure expectedFailure = getDefaultOperationFailure();

        //when
        LaunchFailure result = LaunchFailure
                .operation(expectedOperationType, expectedFailure);

        //then
        assertEquals(
                expectedOperationType,
                result.operationType().orElseThrow()
        );

        assertEquals(
                expectedFailure,
                result.operationFailure().orElseThrow()
        );
    }

    private OperationFailure getDefaultOperationFailure() {
        return new OperationFailure(
                OperationFailureCode.UNKNOWN,
                "Something went wrong",
                Map.of()
        );
    }
}
