package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.architecture.support.recording.RecordingEventBus;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.architecture.support.recording.RecordingNativeExtractionService;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.impl.ExtractNativesOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractNativesOperationTest {
    private RecordingNativeExtractionService nativeExtractionService;
    private SequentialExecutionStrategy sequentialExecutionStrategy;
    private RecordingEventBus eventBus;
    private LaunchContext context;
    private RecordingManifestService recordingManifestService;

    @BeforeEach
    void setUp() {
        nativeExtractionService = new RecordingNativeExtractionService();
        sequentialExecutionStrategy = new SequentialExecutionStrategy();
        eventBus = new RecordingEventBus();
        context = OperationFactoryFixture.getContext();
        recordingManifestService = new RecordingManifestService();
    }

    @Test
    void should_return_failure_when_native_extraction_service_failed() {
        //given
        RuntimeLibrarySelection runtimeLibrarySelection =
                recordingManifestService.loadManifest().runtimeLibrarySelection();

        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        nativeExtractionService.setWithError("Failed to extract natives");

        ExtractNativesOperation operation = new ExtractNativesOperation(
                context,
                sequentialExecutionStrategy,
                eventBus,
                nativeExtractionService,
                OperationFactoryFixture.nativeExtractionPlanBuilder()
        );

        //when
        OperationResult result = operation.execute();

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().orElseThrow().contains("Failed to extract natives"));
    }

    @Test
    void should_return_failure_when_task_is_not_completed() {
        //given
        ExtractNativesOperation operation = new ExtractNativesOperation(
                context,
                sequentialExecutionStrategy,
                eventBus,
                nativeExtractionService,
                OperationFactoryFixture.nativeExtractionPlanBuilder()
        );

        //when
        OperationResult result = operation.execute();

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().orElseThrow().contains("Runtime library selection not available"));
    }

    @Test
    void should_return_success_when_task_is_successful() {
        //given
        RuntimeLibrarySelection runtimeLibrarySelection =
                recordingManifestService.loadManifest().runtimeLibrarySelection();

        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        ExtractNativesOperation operation = new ExtractNativesOperation(
                context,
                sequentialExecutionStrategy,
                eventBus,
                nativeExtractionService,
                OperationFactoryFixture.nativeExtractionPlanBuilder()
        );

        //when
        OperationResult result = operation.execute();

        //then
        assertTrue(result.isSuccess());
    }

}
