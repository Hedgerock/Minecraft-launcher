package com.launcher.core.task;

import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;

public final class TaskResult {

    private TaskResult() {}

    public static Result success() {
        return SuccessResult.INSTANCE;
    }

    public static Result failure(String message) {
        return new FailureResult(message);
    }
}
