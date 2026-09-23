package com.launcher.app.presentation;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.app.support.NoOpPresentationLaunchCompletionHandler;
import com.launcher.app.support.RecordingLauncherLifecycleRunner;
import com.launcher.app.support.RecordingPresentationLaunchCompletionHandler;
import com.launcher.core.LaunchFailure;
import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPresentationLaunchBoundaryTest {

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
                () -> launchResult, handler, executorService
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
                () -> launchResult, handler, executorService
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
        LauncherLifecycleRunner runner = () -> {
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
        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                handler
        );
    }

    private DefaultPresentationLaunchBoundary createBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler handler,
            ExecutorService executorService
    ) {
        return new DefaultPresentationLaunchBoundary(
                launcherLifecycleRunner,
                handler,
                executorService
        );
    }
}
