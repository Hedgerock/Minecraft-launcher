package com.launcher.app.presentation;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.app.presentation.phase.NoOpPresentationLaunchPhaseHandler;
import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticSource;
import com.launcher.app.support.NoOpPresentationLaunchCompletionHandler;
import com.launcher.app.support.NoOpPresentationLaunchDiagnosticReporter;
import com.launcher.app.support.RecordingFailingPresentationLaunchDiagnosticReporter;
import com.launcher.app.support.RecordingLauncherLifecycleRunner;
import com.launcher.app.support.RecordingPresentationLaunchCompletionHandler;
import com.launcher.app.support.RecordingPresentationLaunchDiagnosticReporter;
import com.launcher.core.LaunchFailure;
import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPresentationLaunchBoundaryTest {

    @Test
    void should_deliver_phases_before_completion() throws InterruptedException {
        //given
        List<String> calls = new ArrayList<>();

        PresentationLaunchPhaseHandler phaseHandler = phase ->
                calls.add("phase:" + phase);

        PresentationLaunchCompletionHandler completionHandler = completion ->
                calls.add("completion");

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(PresentationLaunchPhase.LOADING_MANIFEST);
            handler.handle(PresentationLaunchPhase.VERIFYING_FILES);

            return LaunchResult.success(LauncherState.RUNNING);
        };

        PresentationLaunchDiagnosticReporter reporter = (source, cause) -> {};

        ExecutorService executorService =
                Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                completionHandler,
                reporter,
                phaseHandler,
                executorService
        )) {
            //when
            boundary.requestLaunch();

            executorService.shutdown();

            assertTrue(
                    executorService.awaitTermination(5, TimeUnit.SECONDS)
            );

            //then
            assertEquals(
                    List.of(
                            "phase:" + PresentationLaunchPhase.LOADING_MANIFEST,
                            "phase:" + PresentationLaunchPhase.VERIFYING_FILES,
                            "completion"
                    ),
                    calls
            );
        }
    }

    @Test
    void should_deliver_original_completion_when_phase_handler_failed() throws InterruptedException {
        //given
        LaunchResult expectedResult =
                LaunchResult.success(LauncherState.RUNNING);

        RuntimeException phaseHandlerFailure =
                new RuntimeException("Phase handler failed");

        PresentationLaunchPhaseHandler failingPhaseHandler = phase -> {
            throw phaseHandlerFailure;
        };

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(PresentationLaunchPhase.LOADING_MANIFEST);

            return expectedResult;
        };

        RecordingPresentationLaunchCompletionHandler completionHandler =
                new RecordingPresentationLaunchCompletionHandler();

        RecordingPresentationLaunchDiagnosticReporter reporter =
                new RecordingPresentationLaunchDiagnosticReporter();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                completionHandler,
                reporter,
                failingPhaseHandler,
                executorService
        )) {
            //when
            LaunchRequestResult requestResult =
                    boundary.requestLaunch();

            assertTrue(
                    completionHandler.awaitHandled(5, TimeUnit.SECONDS)
            );

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    requestResult
            );

            PresentationLaunchCompletion completion =
                    completionHandler.getResult();

            assertFalse(completion.executionFailed());

            assertEquals(
                    Optional.of(expectedResult),
                    completion.launchResult()
            );
        }
    }

    @Test
    void should_accept_new_request_when_phase_handler_and_diagnostic_reporter_failed()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        RecordingPresentationLaunchCompletionHandler completionHandler =
                new RecordingPresentationLaunchCompletionHandler();

        RuntimeException phaseHandlerFailure =
                new RuntimeException("Phase handler failed");

        PresentationLaunchPhaseHandler phaseHandler = phase -> {
            throw phaseHandlerFailure;
        };

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(PresentationLaunchPhase.LOADING_MANIFEST);

            return LaunchResult.success(LauncherState.RUNNING);
        };

        RecordingFailingPresentationLaunchDiagnosticReporter reporter =
                new RecordingFailingPresentationLaunchDiagnosticReporter();

        ExecutorService executorService =
                Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                completionHandler,
                reporter,
                phaseHandler,
                executorService
        )) {
            LaunchRequestResult firstResult = boundary.requestLaunch();

            executorService.submit(() -> {})
                    .get(5, TimeUnit.SECONDS);

            LaunchRequestResult secondResult = boundary.requestLaunch();

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    firstResult
            );

            assertEquals(
                    PresentationLaunchDiagnosticSource.PHASE_HANDLER,
                    reporter.getSource()
            );

            assertSame(
                    phaseHandlerFailure,
                    reporter.getCause()
            );

            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    secondResult
            );
        }
    }

    @Test
    void should_accept_new_request_when_phase_handler_failed()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        RecordingPresentationLaunchCompletionHandler completionHandler =
                new RecordingPresentationLaunchCompletionHandler();

        RuntimeException phaseHandlerFailure =
                new RuntimeException("Phase handler failed");

        PresentationLaunchPhaseHandler phaseHandler = phase -> {
            throw phaseHandlerFailure;
        };

        LauncherLifecycleRunner runner = handler -> {
            handler.handle(PresentationLaunchPhase.LOADING_MANIFEST);

            return LaunchResult.success(LauncherState.RUNNING);
        };

        RecordingPresentationLaunchDiagnosticReporter reporter =
                new RecordingPresentationLaunchDiagnosticReporter();

        ExecutorService executorService =
                Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                completionHandler,
                reporter,
                phaseHandler,
                executorService
        )) {
            LaunchRequestResult firstResult = boundary.requestLaunch();

            executorService.submit(() -> {})
                    .get(5, TimeUnit.SECONDS);

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    firstResult
            );

            assertEquals(
                    PresentationLaunchDiagnosticSource.PHASE_HANDLER,
                    reporter.getSource()
            );

            assertSame(
                    phaseHandlerFailure,
                    reporter.getCause()
            );

            //when
            LaunchRequestResult secondResult = boundary.requestLaunch();

            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    secondResult
            );
        }
    }

    @Test
    void should_accept_new_request_when_completion_handler_and_diagnostic_reporter_failed()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        LauncherLifecycleRunner runner =
                handler -> LaunchResult.success(LauncherState.RUNNING);

        RuntimeException handlerFailure =
                new RuntimeException("Completion handler failed");

        PresentationLaunchCompletionHandler failingHandler = completion -> {
            throw handlerFailure;
        };

        RecordingFailingPresentationLaunchDiagnosticReporter failingReporter =
                new RecordingFailingPresentationLaunchDiagnosticReporter();

        ExecutorService executorService =
                Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                failingHandler,
                failingReporter,
                new NoOpPresentationLaunchPhaseHandler(),
                executorService
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();

            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            LaunchRequestResult secondResult = boundary.requestLaunch();

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    firstResult
            );

            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    secondResult
            );

            assertEquals(
                    PresentationLaunchDiagnosticSource.COMPLETION_HANDLER,
                    failingReporter.getSource()
            );

            assertSame(
                    handlerFailure,
                    failingReporter.getCause()
            );
        }
    }

    @Test
    void should_deliver_execution_failure_when_diagnostic_reporter_failed()
    throws InterruptedException, TimeoutException, ExecutionException {
        //given
        RuntimeException cause = new RuntimeException("Failed to launch");

        LauncherLifecycleRunner failingRunner = handler -> {
            throw cause;
        };

        RecordingPresentationLaunchCompletionHandler completionHandler =
                new RecordingPresentationLaunchCompletionHandler();

        RecordingFailingPresentationLaunchDiagnosticReporter failingReporter =
                new RecordingFailingPresentationLaunchDiagnosticReporter();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                failingRunner,
                completionHandler,
                failingReporter,
                new NoOpPresentationLaunchPhaseHandler(),
                executorService
        )) {
            //when
            LaunchRequestResult requestResult = boundary.requestLaunch();

            assertTrue(
                    completionHandler.awaitHandled(5, TimeUnit.SECONDS)
            );

            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            LaunchRequestResult secondResult = boundary.requestLaunch();

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    requestResult
            );

            PresentationLaunchCompletion completion = completionHandler.getResult();

            assertTrue(completion.executionFailed());
            assertTrue(completion.launchResult().isEmpty());

            assertEquals(
                    PresentationLaunchDiagnosticSource.LAUNCH_EXECUTION,
                    failingReporter.getSource()
            );

            assertSame(cause, failingReporter.getCause());

            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    secondResult
            );
        }
    }

    @Test
    void should_reject_second_request_while_completion_handler_is_running()
            throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicReference<PresentationLaunchCompletion> currentCompletion = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch canComplete = new CountDownLatch(1);

        PresentationLaunchCompletionHandler handler = completion -> {
            currentCompletion.set(completion);
            started.countDown();
            try {
                canComplete.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };

        LaunchResult launchResult = LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle("Failed to run")
        );

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                phaseHandler ->
                        launchResult, handler, executorService
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();

            try {
                assertTrue(started.await(5, TimeUnit.SECONDS));

                LaunchRequestResult secondResult = boundary.requestLaunch();

                //then
                assertEquals(LaunchRequestResult.ACCEPTED, firstResult);
                assertEquals(LaunchRequestResult.REJECTED_ALREADY_RUNNING, secondResult);
                assertSame(launchResult, currentCompletion.get().launchResult().orElseThrow());

            } finally {
                canComplete.countDown();
            }
        }
    }

    @Test
    void should_not_deliver_second_completion_when_handler_throws()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<PresentationLaunchCompletion> firstCompletion = new AtomicReference<>();
        LaunchResult launchResult = LaunchResult.failure(
                LauncherState.FAILED,
                LaunchFailure.lifecycle("Failed to run")
        );

        PresentationLaunchCompletionHandler handler = completion -> {
            if (calls.incrementAndGet() == 1) {
                firstCompletion.set(completion);
                throw new IllegalStateException("handler failed");
            }
        };

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                phaseHandler ->
                        launchResult, handler, executorService
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();
            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, firstResult);
            assertEquals(1, calls.get());
            assertSame(launchResult, firstCompletion.get().launchResult().orElseThrow());

            //when & then
            assertEquals(LaunchRequestResult.ACCEPTED, boundary.requestLaunch());
        }
    }

    @Test
    void should_deliver_execution_failure_when_runner_throws() throws InterruptedException {
        //given
        LauncherLifecycleRunner runner = handler -> {
            throw new IllegalStateException("internal path: error");
        };

        RecordingPresentationLaunchCompletionHandler handler =
                new RecordingPresentationLaunchCompletionHandler();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(runner, handler)) {
            //when
            LaunchRequestResult requestResult = boundary.requestLaunch();
            assertTrue(handler.awaitHandled(5, TimeUnit.SECONDS));

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, requestResult);
            assertTrue(handler.getResult().executionFailed());
            assertTrue(handler.getResult().launchResult().isEmpty());
        }
    }

    @Test
    void should_fail_when_launch_request_cannot_be_submitted() {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DefaultPresentationLaunchBoundary boundary = createBoundary(
                new RecordingLauncherLifecycleRunner(
                        LaunchResult.failure(
                                LauncherState.FAILED,
                                LaunchFailure.lifecycle("failed to run")
                        )
                ),
                new NoOpPresentationLaunchCompletionHandler(),
                executorService
        );

        executorService.shutdown();

        //when & then
        assertThrows(
                RejectedExecutionException.class,
                boundary::requestLaunch
        );

    }

    @Test
    void should_accept_new_request_after_previous_launch_failed()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                new RecordingLauncherLifecycleRunner(
                        LaunchResult.failure(
                                LauncherState.FAILED,
                                LaunchFailure.lifecycle("failed to run")
                        )
                ),
                new NoOpPresentationLaunchCompletionHandler(),
                executorService
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();

            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            LaunchRequestResult secondResult = boundary.requestLaunch();

            //then
            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    firstResult
            );

            assertEquals(
                    LaunchRequestResult.ACCEPTED,
                    secondResult
            );
        }
    }

    @Test
    void should_shutdown_executor_when_closed() {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        DefaultPresentationLaunchBoundary boundary = createBoundary(
                new RecordingLauncherLifecycleRunner(
                        LaunchResult.success(LauncherState.RUNNING)
                ),
                new NoOpPresentationLaunchCompletionHandler(),
                executorService
        );

        //when
        boundary.close();

        //then
        assertTrue(executorService.isShutdown());
    }

    @Test
    void should_accept_new_request_after_previous_launch_completed()
            throws InterruptedException, TimeoutException, ExecutionException {
        //given
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch canComplete = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        BlockingLauncherLifecycleRunner runner = new BlockingLauncherLifecycleRunner(started, canComplete);

        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                new NoOpPresentationLaunchCompletionHandler(),
                executorService
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();

            try {
                assertTrue(started.await(5, TimeUnit.SECONDS));
            } finally {
                canComplete.countDown();
            }

            executorService.submit(() -> {}).get(5, TimeUnit.SECONDS);

            LaunchRequestResult secondResult = boundary.requestLaunch();

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, firstResult);
            assertEquals(LaunchRequestResult.ACCEPTED, secondResult);
        }
    }

    @Test
    void should_deliver_launch_result_to_result_handler() throws InterruptedException {
        //given
        RecordingLauncherLifecycleRunner runner = new RecordingLauncherLifecycleRunner(
                LaunchResult.success(LauncherState.RUNNING)
        );
        RecordingPresentationLaunchCompletionHandler handler = new RecordingPresentationLaunchCompletionHandler();
        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                runner,
                handler
        )) {
            //when
            LaunchRequestResult result = boundary.requestLaunch();

            assertTrue(handler.awaitHandled(5, TimeUnit.SECONDS));

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, result);
            assertEquals(
                    runner.getLaunchResult(),
                    handler.getResult().launchResult().orElseThrow()
            );
        }
    }

    @Test
    void should_reject_second_request_when_launch_is_running() throws InterruptedException {
        //given
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch canComplete = new CountDownLatch(1);
        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                new BlockingLauncherLifecycleRunner(started, canComplete),
                new NoOpPresentationLaunchCompletionHandler()
        )) {
            //when
            LaunchRequestResult firstResult = boundary.requestLaunch();

            LaunchRequestResult secondResult;
            try {
                assertTrue(started.await(5, TimeUnit.SECONDS));
                secondResult = boundary.requestLaunch();
            } finally {
                canComplete.countDown();
            }

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, firstResult);
            assertEquals(LaunchRequestResult.REJECTED_ALREADY_RUNNING, secondResult);
        }
    }

    @Test
    void should_accept_first_launch_request() {
        //given
        try (DefaultPresentationLaunchBoundary boundary = createBoundary(
                new RecordingLauncherLifecycleRunner(
                        LaunchResult.success(LauncherState.RUNNING)
                ),
                new NoOpPresentationLaunchCompletionHandler()
        )) {
            //when
            LaunchRequestResult result = boundary.requestLaunch();

            //then
            assertEquals(LaunchRequestResult.ACCEPTED, result);
        }
    }

    @Test
    void should_reject_null_presentation_launch_diagnostic_reporter() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> createBoundary(
                        new RecordingLauncherLifecycleRunner(
                                LaunchResult.success(LauncherState.RUNNING)
                        ),
                        new NoOpPresentationLaunchCompletionHandler(),
                        null,
                        new NoOpPresentationLaunchPhaseHandler()
                )
        );

        assertEquals("presentationLaunchDiagnosticReporter", exception.getMessage());
    }

    @Test
    void should_reject_null_executor_service() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> createBoundary(
                        new RecordingLauncherLifecycleRunner(
                                LaunchResult.success(LauncherState.RUNNING)
                        ),
                        new NoOpPresentationLaunchCompletionHandler(),
                        null
                )
        );

        assertEquals("executorService", exception.getMessage());
    }

    @Test
    void should_reject_null_phase_handler() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> createBoundary(
                        new RecordingLauncherLifecycleRunner(
                                LaunchResult.success(LauncherState.RUNNING)
                        ),
                        new NoOpPresentationLaunchCompletionHandler(),
                        new NoOpPresentationLaunchDiagnosticReporter(),
                        null
                )
        );

        assertEquals("presentationLaunchPhaseHandler", exception.getMessage());
    }

    @Test
    void should_reject_null_launcher_result_handler() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> createBoundary(
                        new RecordingLauncherLifecycleRunner(
                                LaunchResult.success(LauncherState.RUNNING)
                        ),
                        null
                )
        );

        assertEquals("presentationLaunchCompletionHandler", exception.getMessage());
    }

    @Test
    void should_reject_null_launcher_lifecycle_runner() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> createBoundary(
                        null,
                        new NoOpPresentationLaunchCompletionHandler()
                )
        );

        assertEquals("launcherLifecycleRunner", exception.getMessage());
    }

    private DefaultPresentationLaunchBoundary createBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler handler
    ) {
        return createBoundary(
                launcherLifecycleRunner,
                handler,
                new NoOpPresentationLaunchDiagnosticReporter(),
                new NoOpPresentationLaunchPhaseHandler()
        );
    }

    private DefaultPresentationLaunchBoundary createBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler handler,
            PresentationLaunchDiagnosticReporter reporter,
            PresentationLaunchPhaseHandler phaseHandler
    ) {
        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                handler,
                reporter,
                phaseHandler
        );
    }

    private DefaultPresentationLaunchBoundary createBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler handler,
            PresentationLaunchDiagnosticReporter reporter,
            PresentationLaunchPhaseHandler phaseHandler,
            ExecutorService executorService
    ) {
        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                handler,
                reporter,
                phaseHandler,
                executorService
        );
    }

    private DefaultPresentationLaunchBoundary createBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler handler,
            ExecutorService executorService
    ) {
        return createBoundary(
                launcherLifecycleRunner,
                handler,
                new NoOpPresentationLaunchDiagnosticReporter(),
                new NoOpPresentationLaunchPhaseHandler(),
                executorService
        );
    }
}
