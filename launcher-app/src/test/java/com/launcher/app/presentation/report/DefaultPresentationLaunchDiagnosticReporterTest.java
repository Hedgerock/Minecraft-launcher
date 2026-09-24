package com.launcher.app.presentation.report;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultPresentationLaunchDiagnosticReporterTest {

    @Test
    void should_reject_null_logger() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultPresentationLaunchDiagnosticReporter(null)
        );

        assertEquals("logger", exception.getMessage());
    }

    @Test
    void should_reject_null_cause() {
        //given
        DefaultPresentationLaunchDiagnosticReporter reporter =
                new DefaultPresentationLaunchDiagnosticReporter();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reporter.report(
                        PresentationLaunchDiagnosticSource.COMPLETION_HANDLER,
                        null
                )
        );

        assertEquals(
                "cause",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_presentation_launch_diagnostic_source() {
        //given
        DefaultPresentationLaunchDiagnosticReporter reporter = new DefaultPresentationLaunchDiagnosticReporter();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reporter.report(null, new RuntimeException())
        );

        assertEquals(
                "source",
                exception.getMessage()
        );
    }

    @Test
    void should_log_launch_diagnostic_for_completion_handler_without_sensitive_information() {
        //given
        Logger logger = getAnonymousLogger();

        RecordingLogHandler logHandler = new RecordingLogHandler();

        logger.addHandler(logHandler);

        PresentationLaunchDiagnosticReporter reporter =
                new DefaultPresentationLaunchDiagnosticReporter(logger);

        Throwable cause = new RuntimeException(
                "Failed to complete download from " +
                        "https://cdn.example.test/game/client.jar?token=secret-token"
        );

        PresentationLaunchDiagnosticSource source =
                PresentationLaunchDiagnosticSource.COMPLETION_HANDLER;

        String throwableName = cause.getClass().getName();

        //when
        reporter.report(
                source,
                cause
        );

        //then
        LogRecord record = logHandler.getRecord();

        assertLogRecord(
                record,
                source,
                throwableName
        );
    }

    @Test
    void should_log_launch_diagnostic_for_launch_execution_without_sensitive_information() {
        //given
        Logger logger = getAnonymousLogger();

        RecordingLogHandler logHandler = new RecordingLogHandler();

        logger.addHandler(logHandler);

        PresentationLaunchDiagnosticReporter reporter =
                new DefaultPresentationLaunchDiagnosticReporter(logger);

        Throwable cause = new RuntimeException(
                "Failed to access C:\\Users\\Player\\secret\\token.txt"
        );

        String throwableName = cause.getClass().getName();

        PresentationLaunchDiagnosticSource source =
                PresentationLaunchDiagnosticSource.LAUNCH_EXECUTION;

        //when
        reporter.report(
                source,
                cause
        );

        //then
        LogRecord record = logHandler.getRecord();

        assertLogRecord(
                record,
                source,
                throwableName
        );
    }

    private void assertLogRecord(
            LogRecord record,
            PresentationLaunchDiagnosticSource source,
            String throwableName
    ) {
        assertNotNull(record);

        assertEquals(
                Level.SEVERE,
                record.getLevel()
        );

        assertEquals(
                "Launch failure [%s]: %s".formatted(source, throwableName),
                record.getMessage()
        );

        assertNull(record.getThrown());
    }

    private Logger getAnonymousLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        return logger;
    }
}
