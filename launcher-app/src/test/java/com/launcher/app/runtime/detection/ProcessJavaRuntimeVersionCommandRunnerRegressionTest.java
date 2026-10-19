package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(15)
class ProcessJavaRuntimeVersionCommandRunnerRegressionTest {

    private static final JavaExecutableReference REFERENCE =
            JavaExecutableReference.explicitPath("runtime/java/bin/java");

    @Test
    void should_accept_output_at_byte_limit() {
        //given
        String output = "é".repeat(4);
        ProbeProcess process = new ProbeProcess(stream(output), false);

        ProcessJavaRuntimeVersionCommandRunner runner = runner(
                process,
                Duration.ofSeconds(5),
                8
        );

        try {
            //when
            JavaRuntimeVersionCommandResult result = runner.run(REFERENCE);

            //then
            assertEquals(0, result.exitCode());
            assertEquals(output, result.output());
            assertFalse(process.forceRequested);
            assertResourcesReleased(process);
        } finally {
            process.destroy();
        }
    }

    @Test
    void should_fail_and_cleanup_when_output_exceeds_byte_limit() {
        //given
        ProbeProcess process = new ProbeProcess(
                stream("é".repeat(4) + "x"),
                true
        );

        ProcessJavaRuntimeVersionCommandRunner runner =
                runner(
                        process,
                        Duration.ofSeconds(5),
                        8
                );

        try {
            //when & then
            assertFailureAndCleanup(
                    runner,
                    process,
                    "Java process output exceeded the byte limit"
            );
        } finally {
            process.destroy();
        }
    }

    @Test
    void should_fail_and_cleanup_when_output_reading_fails() {
        //given
        InputStream brokenOutput = new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("Internal test details");
            }
        };

        ProbeProcess process = new ProbeProcess(
                brokenOutput,
                true
        );

        ProcessJavaRuntimeVersionCommandRunner runner = runner(
                process,
                Duration.ofSeconds(5),
                1024
        );

        try {
            //when & then
            assertFailureAndCleanup(
                    runner,
                    process,
                    "Java process output could not be read"
            );
        } finally {
            process.destroy();
        }
    }

    @Test
    void should_fail_and_cleanup_when_output_does_not_finish() {
        //given
        ProbeProcess process = new ProbeProcess(
                new BlockingInputStream(),
                true
        );

        ProcessJavaRuntimeVersionCommandRunner runner = runner(
                process,
                Duration.ofMillis(200),
                1024
        );

        try {
            //when & then
            assertFailureAndCleanup(
                    runner,
                    process,
                    "Java process timed out"
            );
        } finally {
            process.destroy();
        }
    }

    @Test
    void should_fail_and_cleanup_when_process_does_not_exit() {
        //given
        ProbeProcess process = new ProbeProcess(
                stream("version output"),
                true
        );

        ProcessJavaRuntimeVersionCommandRunner runner =
                runner(
                        process,
                        Duration.ofMillis(200),
                        1024
                );

        try {
            //when & then
            assertFailureAndCleanup(
                    runner,
                    process,
                    "Java process timed out"
            );
        } finally {
            process.destroy();
        }
    }

    @Test
    void should_preserve_execution_failure_when_cleanup_times_out() {
        //given
        ProbeProcess probeProcess = new ProbeProcess(
                stream("version output"),
                true
        ) {
            @Override
            public Process destroyForcibly() {
                return this;
            }
        };

        ProcessJavaRuntimeVersionCommandRunner runner =
                new ProcessJavaRuntimeVersionCommandRunner(
                        reference -> probeProcess,
                        new JavaRuntimeVersionProbeLimits(
                                Duration.ofMillis(200),
                                1024,
                                Duration.ofMillis(200)
                        )
                );

        try {
            //when
            JavaRuntimeVersionDetectionException exception = assertThrows(
                    JavaRuntimeVersionDetectionException.class,
                    () -> runner.run(REFERENCE)
            );

            //then
            assertEquals(
                    "Java process timed out",
                    exception.getMessage()
            );

            assertEquals(
                    1,
                    exception.getSuppressed().length
            );

            Throwable cleanupFailure = exception.getSuppressed()[0];

            assertInstanceOf(
                    JavaRuntimeVersionDetectionException.class,
                    cleanupFailure
            );

            assertEquals(
                    "Java process cleanup did not complete",
                    cleanupFailure.getMessage()
            );
            assertTrue(probeProcess.isAlive());
        } finally {
            probeProcess.destroy();
        }
    }

    private ProcessJavaRuntimeVersionCommandRunner runner(
            ProbeProcess probeProcess,
            Duration executionTimeout,
            int maxOutputBytes
    ) {
        return new ProcessJavaRuntimeVersionCommandRunner(
                reference -> probeProcess,
                new JavaRuntimeVersionProbeLimits(
                        executionTimeout,
                        maxOutputBytes,
                        Duration.ofSeconds(2)
                )
        );
    }

    private void assertFailureAndCleanup(
            ProcessJavaRuntimeVersionCommandRunner runner,
            ProbeProcess process,
            String expectedMessage
    ) {
        JavaRuntimeVersionDetectionException exception = assertThrows(
                JavaRuntimeVersionDetectionException.class,
                () -> runner.run(REFERENCE)
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(0, exception.getSuppressed().length);
        assertTrue(process.forceRequested);
        assertFalse(process.isAlive());
        assertResourcesReleased(process);
    }

    private void assertResourcesReleased(ProbeProcess process) {
        assertTrue(process.output.closed);
        assertTrue(process.error.closed);
        assertTrue(process.input.closed);

        assertNotNull(process.readerThread);
        assertFalse(process.readerThread.isAlive());
    }

    private static InputStream stream(String value) {
        return new ByteArrayInputStream(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static class ProbeProcess extends Process {

        private final TrackingInputStream output;
        private final TrackingInputStream error =
                new TrackingInputStream(InputStream.nullInputStream());
        private final TrackingOutputStream input =
                new TrackingOutputStream();
        private final CountDownLatch terminated;

        private volatile boolean forceRequested;
        private volatile Thread readerThread;

        private ProbeProcess(InputStream output, boolean running) {
            this.output = new TrackingInputStream(output);
            this.terminated = new CountDownLatch(running ? 1 : 0);
        }

        @Override
        public OutputStream getOutputStream() {
            return input;
        }

        @Override
        public InputStream getInputStream() {
            readerThread = Thread.currentThread();
            return output;
        }

        @Override
        public InputStream getErrorStream() {
            return error;
        }

        @Override
        public int waitFor() throws InterruptedException {
            terminated.await();
            return 0;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            return terminated.await(timeout, unit);
        }

        @Override
        public boolean isAlive() {
            return terminated.getCount() != 0;
        }

        @Override
        public int exitValue() {
            if (isAlive()) {
                throw new IllegalThreadStateException(
                        "The process is still alive"
                );
            }

            return 0;
        }

        @Override
        public void destroy() {
            try (output; error; input) {
                terminated.countDown();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Process destroyForcibly() {
            forceRequested = true;
            destroy();
            return this;
        }
    }

    private static final class TrackingInputStream extends FilterInputStream {
        private volatile boolean closed;

        private TrackingInputStream(InputStream delegate) {
            super(delegate);
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    private static final class TrackingOutputStream extends FilterOutputStream {
        private volatile boolean closed;

        private TrackingOutputStream() {
            super(OutputStream.nullOutputStream());
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    private static final class BlockingInputStream extends InputStream {

        private final CountDownLatch released = new CountDownLatch(1);

        @Override
        public int read() throws IOException {
            try {
                released.await();
                return -1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Test read was interrupted", e);
            }
        }

        @Override
        public void close() {
            released.countDown();
        }
    }
}
