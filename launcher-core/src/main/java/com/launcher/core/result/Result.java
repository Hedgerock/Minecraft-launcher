package com.launcher.core.result;

public sealed interface Result permits SuccessResult, FailureResult {
    boolean success();
}
