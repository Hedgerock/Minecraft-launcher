package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.FixedResultExecutionStrategy;
import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.impl.*;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OperationFactoryTest {
    private OperationFactoryFixture operationFactoryFixture;
    private LaunchContext context;

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
        operationFactoryFixture = new OperationFactoryFixture();
    }

    @Test
    void should_create_load_manifest_operation_for_load_manifest_type() {
        //given
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

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
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

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
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

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
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

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
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

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
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

        //when
        LaunchOperation operation = factory.create(
                OperationType.PREPARE_DIRECTORIES,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(PrepareDirectoriesOperation.class, operation);
    }

    @Test
    void should_create_build_game_launch_plan_operation_for_build_game_launch_plan_type() {
        //given
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

        //when
        LaunchOperation operation = factory.create(
                OperationType.BUILD_GAME_LAUNCH_PLAN,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(BuildGameLaunchPlanOperation.class, operation);
    }

    @Test
    void should_create_launch_game_operation_for_launch_game_type() {
        //given
        DefaultOperationFactory factory = operationFactoryFixture.getFactory();

        //when
        LaunchOperation operation = factory.create(
                OperationType.LAUNCH_GAME,
                context,
                new FixedResultExecutionStrategy(OperationResult.success())
        );

        //then
        assertInstanceOf(LaunchGameOperation.class, operation);
    }

}
