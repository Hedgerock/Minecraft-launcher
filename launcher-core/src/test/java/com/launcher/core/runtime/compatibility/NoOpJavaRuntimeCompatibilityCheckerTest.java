package com.launcher.core.runtime.compatibility;

import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;
import com.launcher.model.runtime.JavaRuntimeVersion;
import com.launcher.model.runtime.JavaVersionRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoOpJavaRuntimeCompatibilityCheckerTest {
    private final NoOpJavaRuntimeCompatibilityChecker checker = new NoOpJavaRuntimeCompatibilityChecker();

    @Test
    void should_accept_request() {
        //given
        JavaRuntimeCompatibilityRequest request = new JavaRuntimeCompatibilityRequest(
                new JavaRuntimeVersion(21),
                new JavaVersionRequirement(17)
        );

        //when & then
        assertDoesNotThrow(() -> checker.checkCompatible(request));
    }

    @Test
    void should_reject_null_request() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checker.checkCompatible(null)
        );

        assertEquals(
                "request",
                exception.getMessage()
        );
    }

}
