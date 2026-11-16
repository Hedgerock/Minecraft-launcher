package com.launcher.api.http;

import java.util.concurrent.Flow.Subscription;

final class RecordingSubscription implements Subscription {
    private long request = 0;
    private boolean cancelled = false;
    @Override
    public void request(long n) {
        request += n;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    public long getRequest() {
        return request;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
