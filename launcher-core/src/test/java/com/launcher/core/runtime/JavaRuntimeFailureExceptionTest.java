package com.launcher.core.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaRuntimeFailureExceptionTest {

    @Test
    void should_reject_null_reason() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new TestJavaRuntimeFailureException(null, "message")
        );

        assertEquals("reason", exception.getMessage());
    }

    private static final class TestJavaRuntimeFailureException extends JavaRuntimeFailureException {

        private TestJavaRuntimeFailureException(
                JavaRuntimeFailureReason reason,
                String message
        ) {
            super(reason, message);
        }
    }
}
