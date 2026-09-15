package com.launcher.core.operation.failure;

import java.util.Map;
import java.util.Objects;

public final class OperationFailureMapper {

    public OperationFailure map(Exception exception) {
        Objects.requireNonNull(exception, "exception");

        String message = exception.getMessage();
        boolean isValidMessage =
                message != null &&
                !message.isBlank();

        if (!isValidMessage) {
            return new OperationFailure(
                    OperationFailureCode.UNKNOWN,
                    exception.getClass().getSimpleName(),
                    Map.of()
            );
        }

        return new OperationFailure(
                OperationFailureCode.UNKNOWN,
                message,
                Map.of()
        );
    }
}
