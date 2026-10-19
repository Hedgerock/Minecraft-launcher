package com.launcher.app.runtime.detection;

import com.launcher.model.runtime.JavaExecutableReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class ProcessJavaRuntimeVersionCommandRunner implements JavaRuntimeVersionCommandRunner {
    private final JavaRuntimeVersionProcessStarter processStarter;
    private final JavaRuntimeVersionProbeLimits limits;

    ProcessJavaRuntimeVersionCommandRunner() {
        this(
                new ProcessBuilderJavaRuntimeVersionProcessStarter(),
                JavaRuntimeVersionProbeLimits.defaults()
        );
    }

    ProcessJavaRuntimeVersionCommandRunner(
            JavaRuntimeVersionProcessStarter processStarter,
            JavaRuntimeVersionProbeLimits limits
    ) {
        this.processStarter = Objects.requireNonNull(processStarter, "processStarter");
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    @Override
    public JavaRuntimeVersionCommandResult run(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        if (Thread.currentThread().isInterrupted()) {
            throw detectionFailure(
                    JavaProcessDiagnostic.processInterrupted()
            );
        }

        Process process = startProcess(javaExecutableReference);
        long executionStartedAt = System.nanoTime();

        Thread reader = null;
        Throwable executionFailure = null;

        try {
            FutureTask<byte[]> outputTask = new FutureTask<>(
                    () -> readOutput(process)
            );

            reader = new Thread(
                    outputTask,
                    "java-version-output-reader"
            );
            reader.setDaemon(true);
            reader.start();

            return awaitResult(
                    process,
                    outputTask,
                    executionStartedAt
            );
        } catch (RuntimeException | Error failure) {
            executionFailure = failure;
            throw failure;
        } finally {
            try {
                cleanup(process, reader);

                if (executionFailure == null && Thread.currentThread().isInterrupted()) {
                    throw detectionFailure(
                            JavaProcessDiagnostic.processInterrupted()
                    );
                }
            } catch (RuntimeException | Error cleanupFailure) {
                if (executionFailure == null) {
                    throw cleanupFailure;
                }

                if (executionFailure != cleanupFailure) {
                    executionFailure.addSuppressed(cleanupFailure);
                }
            }
        }
    }

    private Process startProcess(
            JavaExecutableReference javaExecutableReference
    ) {
        try {
            return processStarter.start(javaExecutableReference);
        } catch (IOException e) {
            throw detectionFailure(
                    JavaProcessDiagnostic.processStartFailed()
            );
        }
    }

    private byte[] readOutput(Process process) throws IOException {
        try (
                InputStream output = process.getInputStream();
                InputStream error = process.getErrorStream()
        ) {
            process.getOutputStream().close();

            return output.readNBytes(limits.maxOutputBytes() + 1);
        }
    }

    private JavaRuntimeVersionCommandResult awaitResult(
            Process process,
            FutureTask<byte[]> outputTask,
            long executionStartedAt
    ) {
        try {
            byte[] output = outputTask.get(
                    remainingExecutionNanos(executionStartedAt),
                    TimeUnit.NANOSECONDS
            );

            if (output.length > limits.maxOutputBytes()) {
                throw detectionFailure(
                        JavaProcessDiagnostic.outputLimitExceeded()
                );
            }

            boolean finished = process.waitFor(
                    remainingExecutionNanos(executionStartedAt),
                    TimeUnit.NANOSECONDS
            );

            if (!finished) {
                throw detectionFailure(
                        JavaProcessDiagnostic.processTimedOut()
                );
            }

            return new JavaRuntimeVersionCommandResult(
                    process.exitValue(),
                    new String(output, StandardCharsets.UTF_8)
            );
        } catch (TimeoutException e) {
            throw detectionFailure(
                    JavaProcessDiagnostic.processTimedOut()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw detectionFailure(
                    JavaProcessDiagnostic.processInterrupted()
            );
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw detectionFailure(
                    JavaProcessDiagnostic.outputReadFailed()
            );
        }
    }

    private long remainingExecutionNanos(long executionStartedAt) throws TimeoutException {
        long elapsed = System.nanoTime() - executionStartedAt;
        long remaining = limits.executionTimeout().toNanos() - elapsed;

        if (remaining <= 0) {
            throw new TimeoutException();
        }

        return remaining;
    }

    private void cleanup(Process process, Thread reader) {
        long cleanupStartedAt = System.nanoTime();
        boolean interrupted = Thread.interrupted();

        try {
            if (process.isAlive()) {
                process.destroyForcibly();
            }

            if (reader != null) {
                reader.interrupt();
            }

            while (process.isAlive() || (reader != null && reader.isAlive())) {
                long elapsed = System.nanoTime() - cleanupStartedAt;
                long remaining = limits.cleanupTimeout().toNanos() - elapsed;

                if (remaining <= 0) {
                    throw detectionFailure(
                            JavaProcessDiagnostic.processCleanupFailed()
                    );
                }

                try {
                    if (process.isAlive()) {
                        process.waitFor(
                                remaining,
                                TimeUnit.NANOSECONDS
                        );
                    } else if (reader != null) {
                        TimeUnit.NANOSECONDS.timedJoin(
                                reader,
                                remaining
                        );
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private JavaRuntimeVersionDetectionException detectionFailure(
            JavaProcessDiagnostic diagnostic
    ) {
        return new JavaRuntimeVersionDetectionException(
                diagnostic.message()
        );
    }
}
