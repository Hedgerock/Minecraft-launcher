package com.launcher.core.architecture.operation;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.architecture.support.FixedResultExecutionStrategy;
import com.launcher.core.architecture.support.RecordingManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.impl.LoadManifestOperation;
import com.launcher.core.operation.impl.RepairOperation;
import com.launcher.core.operation.impl.VerificationOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OperationFactoryTest {

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")

                )
        );
    }

    @Test
    void should_create_load_manifest_operation_for_load_manifest_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        DefaultOperationFactory factory = new DefaultOperationFactory(
                service,
                eventBus
        );

        LaunchContext context = getContext();

        //when
        LaunchOperation operation = factory.create(
                OperationType.LOAD_MANIFEST,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(LoadManifestOperation.class, operation);
    }

    @Test
    void should_create_repair_operation_for_repair_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        DefaultOperationFactory factory = new DefaultOperationFactory(
                service,
                eventBus
        );

        LaunchContext context = getContext();

        //when
        LaunchOperation operation = factory.create(
                OperationType.REPAIR,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then

        assertInstanceOf(RepairOperation.class, operation);
    }

    @Test
    void should_create_verification_operation_for_verify_files_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        DefaultOperationFactory factory = new DefaultOperationFactory(
                service,
                eventBus
        );

        LaunchContext context = getContext();

        //when
        LaunchOperation operation = factory.create(
                OperationType.VERIFY_FILES,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then

        assertInstanceOf(VerificationOperation.class, operation);
    }

}
