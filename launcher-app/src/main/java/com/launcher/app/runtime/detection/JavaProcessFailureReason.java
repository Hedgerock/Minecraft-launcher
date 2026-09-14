package com.launcher.app.runtime.detection;

enum JavaProcessFailureReason {
    PROCESS_START_FAILED,
    NON_ZERO_EXIT_CODE,
    EMPTY_OUTPUT,
    OUTPUT_PARSING_FAILED,
    PROCESS_INTERRUPTED
}
