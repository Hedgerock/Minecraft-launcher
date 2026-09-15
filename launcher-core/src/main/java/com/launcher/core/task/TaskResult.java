package com.launcher.core.task;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;

import java.util.Map;

public final class TaskResult {

    private TaskResult() {}

    public static Result success() {
        return SuccessResult.INSTANCE;
    }

    public static Result failure(String message) {
        return failure(
                new OperationFailure(
                        OperationFailureCode.UNKNOWN,
                        message,
                        Map.of()
                )
        );
    }

    public static Result failure(OperationFailure failure) {
        return new FailureResult(failure);
    }
}
