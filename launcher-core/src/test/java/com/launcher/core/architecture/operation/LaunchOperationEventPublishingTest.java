package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.EventPublishingOperation;
import com.launcher.core.architecture.support.FailingWithoutMessageOperation;
import com.launcher.core.architecture.support.FinalizeFailingOperation;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.OperationCompletedEvent;
import com.launcher.core.event.events.OperationFailedEvent;
import com.launcher.core.event.events.OperationStartedEvent;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
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

    @Test
    void should_publish_failed_event_when_finalize_operation_failed() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();
        eventBus.subscribe(
                OperationStartedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("started", event.operationType());
                    events.add(message);
                }
        );

        eventBus.subscribe(
                OperationFailedEvent.class,
                event -> {
                    String message = "%s:%s:%s"
                            .formatted("failed", event.operationType(), event.errorMessage());
                    events.add(message);
                }
        );

        LaunchOperation operation = new FinalizeFailingOperation(
                context,
                eventBus
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:REPAIR",
                        "failed:REPAIR:finalize failed"
                ),
                events
        );
    }

    @Test
    void should_publish_exception_class_name_when_operation_failed_with_error_message() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();
        LaunchContext context = getContext();
        eventBus.subscribe(
                OperationStartedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("started", event.operationType());
                    events.add(message);
                }
        );

        eventBus.subscribe(
                OperationFailedEvent.class,
                event -> {
                    String message = "%s:%s:%s"
                            .formatted("failed", event.operationType(), event.errorMessage());
                    events.add(message);
                }
        );

        LaunchOperation operation = new FailingWithoutMessageOperation(
                context,
                eventBus
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:REPAIR",
                        "failed:REPAIR:IllegalStateException"
                ),
                events
        );
    }

    @Test
    void should_publish_started_and_failed_events_when_operation_failed() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();


        eventBus.subscribe(
                OperationStartedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("started", event.operationType());
                    events.add(message);
                }
        );

        eventBus.subscribe(
                OperationFailedEvent.class,
                event -> {
                    String message = "%s:%s:%s"
                            .formatted("failed", event.operationType(), event.errorMessage());
                    events.add(message);
                }
        );

        LaunchOperation operation = new EventPublishingOperation(
                getContext(),
                eventBus,
                OperationResult.failure("failure")
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:REPAIR",
                        "failed:REPAIR:failure"
                ),
                events
        );
    }

    @Test
    void should_publish_started_and_completed_events_when_operation_succeeded() {
        //given
        EventBus eventBus = new EventBus();
        List<String> events = new ArrayList<>();

        eventBus.subscribe(
                OperationStartedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("started", event.operationType());
                    events.add(message);
                }
        );
        eventBus.subscribe(
                OperationCompletedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("completed", event.operationType());
                    events.add(message);
                }
        );

        LaunchOperation operation = new EventPublishingOperation(
                getContext(),
                eventBus,
                OperationResult.success()
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:REPAIR",
                        "completed:REPAIR"
                ),
                events
        );
    }
}
