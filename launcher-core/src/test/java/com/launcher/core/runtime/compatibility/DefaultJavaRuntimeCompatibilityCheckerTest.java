package com.launcher.core.runtime.compatibility;

import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.core.runtime.compatibility.exception.JavaRuntimeCompatibilityException;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;
import com.launcher.model.runtime.JavaRuntimeVersion;
import com.launcher.model.runtime.JavaVersionRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultJavaRuntimeCompatibilityCheckerTest {
    private final DefaultJavaRuntimeCompatibilityChecker checker = new DefaultJavaRuntimeCompatibilityChecker();
    private static final String TEMPLATE_OF_FAILURE_MESSAGE =
            "Java runtime version %d does not satisfy required Java version %d";

    @Test
    void should_reject_null_java_runtime_compatibility_request() {
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

    @Test
    void should_reject_runtime_version_lower_than_requirement() {
        //given
        int majorVersion = 8;
        int requiredMajorVersion = 17;
        JavaRuntimeCompatibilityRequest request = getRequest(majorVersion, requiredMajorVersion);

        //when & then
        JavaRuntimeCompatibilityException exception = assertThrows(
                JavaRuntimeCompatibilityException.class,
                () -> checker.checkCompatible(request)
        );

        assertEquals(
                JavaRuntimeFailureReason.INCOMPATIBLE_JAVA_VERSION,
                exception.getReason()
        );

        assertEquals(
                String.format(TEMPLATE_OF_FAILURE_MESSAGE, majorVersion, requiredMajorVersion),
                exception.getMessage()
        );
    }

    @Test
    void should_accept_runtime_version_greater_than_requirement() {
        //given
        JavaRuntimeCompatibilityRequest request = getRequest(18, 17);

        //when
        assertDoesNotThrow(
                () -> checker.checkCompatible(request)
        );
    }

    @Test
    void should_accept_runtime_version_equal_to_requirement() {
        //given
        JavaRuntimeCompatibilityRequest request = getRequest(17, 17);

        //when & then
        assertDoesNotThrow(
                () -> checker.checkCompatible(request)
        );
    }

    private JavaRuntimeCompatibilityRequest getRequest(int majorVersion, int requiredMajorVersion) {
        return new JavaRuntimeCompatibilityRequest(
                new JavaRuntimeVersion(majorVersion),
                new JavaVersionRequirement(requiredMajorVersion)
        );
    }
}
