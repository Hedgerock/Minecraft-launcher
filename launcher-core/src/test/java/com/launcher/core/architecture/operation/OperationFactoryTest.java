package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.FixedResultExecutionStrategy;
import com.launcher.core.architecture.support.recording.RecordVerificationService;
import com.launcher.core.architecture.support.recording.RecordingDirectoryService;
import com.launcher.core.architecture.support.recording.RecordingDownloadService;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.impl.*;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.verification.model.VerificationPlan;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OperationFactoryTest {
    private final DownloadPlanBuilder builder = new DownloadPlanBuilder();
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

    private DefaultOperationFactory getDefaultOperationFactory(
            ManifestService manifestService,
            EventBus eventBus
    ) {
        return new DefaultOperationFactory(
                manifestService,
                new RecordVerificationService(getVerificationPlan()),
                new RecordingDownloadService(),
                new RecordingDirectoryService(),
                builder,
                eventBus
        );
    }

    @Test
    void should_create_load_manifest_operation_for_load_manifest_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

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
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

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
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

        //when
        LaunchOperation operation = factory.create(
                OperationType.VERIFY_FILES,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then

        assertInstanceOf(VerificationOperation.class, operation);
    }

    @Test
    void should_create_build_download_plan_operation_for_build_download_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

        //when
        LaunchOperation operation = factory.create(
                OperationType.BUILD_DOWNLOAD_PLAN,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(BuildDownloadPlanOperation.class, operation);
    }

    @Test
    void should_create_download_files_operation_for_download_files_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

        //when
        LaunchOperation operation = factory.create(
                OperationType.DOWNLOAD_FILES,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(DownloadFilesOperation.class, operation);
    }

    @Test
    void should_create_prepare_directories_operation_for_prepare_directories_type() {
        //given
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        LaunchContext context = getContext();
        DefaultOperationFactory factory = getDefaultOperationFactory(service, eventBus);

        //when
        LaunchOperation operation = factory.create(
                OperationType.PREPARE_DIRECTORIES,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(PrepareDirectoriesOperation.class, operation);
    }

}
