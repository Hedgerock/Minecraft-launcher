package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.*;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.OperationCompletedEvent;
import com.launcher.core.event.events.OperationFailedEvent;
import com.launcher.core.event.events.OperationStartedEvent;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildDownloadPlanOperation;
import com.launcher.core.operation.impl.VerificationOperation;
import com.launcher.core.operation.result.OperationResult;
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
    void should_publish_build_download_plan_events_when_build_download_plan_operation_succeeded() {
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
                OperationCompletedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("completed", event.operationType());
                    events.add(message);
                }
        );

        LaunchOperation operation = new BuildDownloadPlanOperation(
                context,
                new FixedResultExecutionStrategy(OperationResult.success()),
                eventBus,
                new DownloadPlanBuilder()
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:BUILD_DOWNLOAD_PLAN",
                        "completed:BUILD_DOWNLOAD_PLAN"
                ),
                events
        );
    }

    @Test
    void should_publish_verify_files_events_when_verification_operation_succeeded() {
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
                OperationCompletedEvent.class,
                event -> {
                    String message = "%s:%s"
                            .formatted("completed", event.operationType());
                    events.add(message);
                }
        );

        LaunchOperation operation = new VerificationOperation(
                context,
                new FixedResultExecutionStrategy(OperationResult.success()),
                eventBus,
                new RecordVerificationService(getVerificationPlan())
        );

        //when
        operation.execute();

        //then
        assertEquals(
                List.of(
                        "started:VERIFY_FILES",
                        "completed:VERIFY_FILES"
                ),
                events
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
