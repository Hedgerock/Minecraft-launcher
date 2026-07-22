package com.launcher.core.task;

import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;

public record TaskResult(
        boolean status,
        String message

) {
    public static Result success() {
        return new SuccessResult();
    }

    public static Result failure(String message) {
        return new FailureResult(message);
    }
}
