package com.launcher.app.presentation.report;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class RecordingLogHandler extends Handler {

    private LogRecord record;

    @Override
    public void publish(LogRecord record) {
        this.record = record;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public LogRecord getRecord() {
        return record;
    }
}
