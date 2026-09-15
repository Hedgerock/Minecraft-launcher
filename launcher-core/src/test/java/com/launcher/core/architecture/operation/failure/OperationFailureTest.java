package com.launcher.core.architecture.operation.failure;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationFailureTest {
    private static final OperationFailureCode DEFAULT_CODE = OperationFailureCode.UNKNOWN;
    private static final String DEFAULT_MESSAGE = "Something went wrong";
    private static final Map<String, String> DEFAULT_DETAILS = Map.of("detail", "value");

    @Test
    void should_create_operation_failure() {
        //given & when
        OperationFailure result = new OperationFailure(DEFAULT_CODE, DEFAULT_MESSAGE, DEFAULT_DETAILS);

        //then
        assertEquals(DEFAULT_CODE, result.code());
        assertEquals(DEFAULT_MESSAGE, result.message());
        assertEquals(DEFAULT_DETAILS, result.details());
    }

    @Test
    void should_reject_details_mutation_from_accessor() {
        //given
        Map<String, String> details = new HashMap<>();
        details.put("secondDetail", "value");

        OperationFailure failure = new OperationFailure(DEFAULT_CODE, DEFAULT_MESSAGE, details);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> failure.details().put("thirdDetail", "value")
        );
    }

    @Test
    void should_create_immutable_details() {
        //given
        Map<String, String> details = new HashMap<>(DEFAULT_DETAILS);
        details.put("secondDetail", "value");

        OperationFailure failure = new OperationFailure(DEFAULT_CODE, DEFAULT_MESSAGE, details);

        //when
        details.put("thirdDetail", "value");

        //then
        assertEquals(
                Map.of("detail", "value", "secondDetail", "value"),
                failure.details()
        );
    }

    @Test
    void should_reject_blank_message() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OperationFailure(DEFAULT_CODE, " ", DEFAULT_DETAILS)
        );

        assertEquals("message cannot be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_details() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OperationFailure(DEFAULT_CODE, DEFAULT_MESSAGE, null)
        );

        assertEquals("details", exception.getMessage());
    }

    @Test
    void should_reject_null_message() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OperationFailure(DEFAULT_CODE, null, DEFAULT_DETAILS)
        );

        assertEquals("message", exception.getMessage());
    }

    @Test
    void should_reject_null_code() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OperationFailure(null, DEFAULT_MESSAGE, DEFAULT_DETAILS)
        );

        assertEquals("code", exception.getMessage());
    }
}
