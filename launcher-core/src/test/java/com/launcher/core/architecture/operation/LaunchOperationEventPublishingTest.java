package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.*;
import com.launcher.core.architecture.support.recording.RecordVerificationService;
import com.launcher.core.architecture.support.recording.RecordingDownloadService;
import com.launcher.core.architecture.support.recording.RecordingEventBus;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.download.DownloadService;
import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.OperationCompletedEvent;
import com.launcher.core.event.events.OperationFailedEvent;
import com.launcher.core.event.events.OperationStartedEvent;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildDownloadPlanOperation;
import com.launcher.core.operation.impl.DownloadFilesOperation;
import com.launcher.core.operation.impl.LaunchGameOperation;
import com.launcher.core.operation.impl.VerificationOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.verification.model.VerificationPlan;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaunchOperationEventPublishingTest {
    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
    }

    private VerificationPlan getVerificationPlan() {
        return new VerificationPlan(
                List.of()
        );
    }

    @Test
    void should_publish_failed_event_when_launch_game_failed() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        LaunchContext context = getContext();
        context.setGameLaunchPlan(
                new GameLaunchPlan(
                        Path.of("game-directory"),
                        List.of("java", "TestMain")
                )
        );

        LaunchOperation operation = new LaunchGameOperation(
                context,
                new SequentialExecutionStrategy(),
                eventBus,
                new FailingGameService()
        );

        //when
        operation.execute();

        //then
        OperationFailedEvent event = eventBus.firstEventOfType(OperationFailedEvent.class);

        assertEquals(OperationType.LAUNCH_GAME, event.operationType());
        assertEquals("Game launch failed", event.errorMessage());

    }

    @Test
    void should_publish_download_files_events_when_download_files_operation_succeeded() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();
        DownloadService service = new RecordingDownloadService();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.COMPLETED, eventBus, events);

        LaunchOperation operation = new DownloadFilesOperation(
                context,
                new FixedResultExecutionStrategy(OperationResult.success()),
                eventBus,
                service
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.COMPLETED, OperationType.DOWNLOAD_FILES);
        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_build_download_plan_events_when_build_download_plan_operation_succeeded() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.COMPLETED, eventBus, events);

        LaunchOperation operation = new BuildDownloadPlanOperation(
                context,
                new FixedResultExecutionStrategy(OperationResult.success()),
                eventBus,
                new DownloadPlanBuilder()
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.COMPLETED, OperationType.BUILD_DOWNLOAD_PLAN);
        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_verify_files_events_when_verification_operation_succeeded() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.COMPLETED, eventBus, events);

        LaunchOperation operation = new VerificationOperation(
                context,
                new FixedResultExecutionStrategy(OperationResult.success()),
                eventBus,
                new RecordVerificationService(getVerificationPlan())
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.COMPLETED, OperationType.VERIFY_FILES);
        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_failed_event_when_finalize_operation_failed() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.FAILED, eventBus, events);

        LaunchOperation operation = new FinalizeFailingOperation(
                context,
                eventBus
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.FAILED, OperationType.REPAIR, "finalize failed");
        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_exception_class_name_when_operation_failed_with_error_message() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.FAILED, eventBus, events);

        LaunchOperation operation = new FailingWithoutMessageOperation(
                context,
                eventBus
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.FAILED, OperationType.REPAIR, "IllegalStateException");

        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_started_and_failed_events_when_operation_failed() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.FAILED, eventBus, events);

        LaunchOperation operation = new EventPublishingOperation(
                getContext(),
                eventBus,
                OperationResult.failure("failure")
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.FAILED, OperationType.REPAIR, "failure");
        assertEquals(expectedEvents, events);
    }

    @Test
    void should_publish_started_and_completed_events_when_operation_succeeded() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();

        subscribe(TestEvents.STARTED, eventBus, events);
        subscribe(TestEvents.COMPLETED, eventBus, events);

        LaunchOperation operation = new EventPublishingOperation(
                getContext(),
                eventBus,
                OperationResult.success()
        );

        //when
        operation.execute();

        //then
        List<String> expectedEvents =
                getExpectedEvents(TestEvents.COMPLETED, OperationType.REPAIR);

        assertEquals(expectedEvents, events);
    }

    @SuppressWarnings("all")
    private List<String> getExpectedEvents(TestEvents currentEvent, OperationType operationType) {
        return getExpectedEvents(currentEvent, operationType, "empty-error-message");
    }

    private List<String> getExpectedEvents(
            TestEvents currentEvent, OperationType operationType, String errorMessage) {
        String startedMessage = "%s:%s".formatted("started", operationType);

        return switch (currentEvent) {
            case STARTED -> List.of(startedMessage);
            case COMPLETED -> {
                String finishedMessage = "%s:%s".formatted("completed", operationType);
                yield List.of(startedMessage, finishedMessage);
            }
            case FAILED -> {
                String finishedMessage = "%s:%s:%s".formatted("failed", operationType, errorMessage);
                yield List.of(startedMessage, finishedMessage);
            }
        };
    }

    private enum TestEvents {
        STARTED,
        COMPLETED,
        FAILED
    }

    @SuppressWarnings("all")
    private void subscribe(TestEvents currentEvent, EventBus eventBus, List<String> events) {

        switch (currentEvent) {
            case STARTED -> {
                eventBus.subscribe(
                        OperationStartedEvent.class,
                        event -> {
                            String message = "%s:%s"
                                    .formatted("started", event.operationType());
                            events.add(message);
                        }
                );
            }
            case COMPLETED -> {
                eventBus.subscribe(
                        OperationCompletedEvent.class,
                        event -> {
                            String message = "%s:%s"
                                    .formatted("completed", event.operationType());
                            events.add(message);
                        }
                );
            }
            case FAILED -> {
                eventBus.subscribe(
                        OperationFailedEvent.class,
                        event -> {
                            String message = "%s:%s:%s"
                                    .formatted("failed", event.operationType(), event.errorMessage());
                            events.add(message);
                        }
                );
            }
        }

    }
}
