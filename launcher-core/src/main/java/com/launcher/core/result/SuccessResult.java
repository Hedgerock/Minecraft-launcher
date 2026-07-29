package com.launcher.core.result;

public final class SuccessResult implements Result {
    public static final SuccessResult INSTANCE = new SuccessResult();
    @Override
    public boolean success() {
        return true;
    }
}
