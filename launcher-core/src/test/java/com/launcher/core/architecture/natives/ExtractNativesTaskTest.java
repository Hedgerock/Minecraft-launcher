package com.launcher.core.architecture.natives;

import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.architecture.support.recording.RecordingNativeExtractionService;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.natives.ExtractNativesTask;
import com.launcher.core.natives.NativeExtractionPlanBuilder;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractNativesTaskTest {
    private RecordingNativeExtractionService nativeExtractionService;
    private NativeExtractionPlanBuilder nativeExtractionPlanBuilder;
    private RecordingManifestService manifestService;
    private LaunchContext context;

    @BeforeEach
    void setUp() {
        nativeExtractionService = new RecordingNativeExtractionService();
        nativeExtractionPlanBuilder = new NativeExtractionPlanBuilder(
                new RecordingDirectoryProvider()
        );
        manifestService = new RecordingManifestService();
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_runtime_library_selection_is_missing() {
        //given
        ExtractNativesTask nativeTask = new ExtractNativesTask(
                nativeExtractionService,
                nativeExtractionPlanBuilder
        );

        //when
        Result result = nativeTask.execute(context);

        //then
        assertInstanceOf(FailureResult.class, result);
        assertTrue(((FailureResult) result).getMessage().contains("Runtime library selection not available"));
    }

    @Test
    void should_use_native_extraction_service_when_artifacts_are_not_empty() {
        //given
        ExtractNativesTask nativeTask = new ExtractNativesTask(
                nativeExtractionService,
                nativeExtractionPlanBuilder
        );

        RuntimeLibrarySelection runtimeLibrarySelection = manifestService.
                loadManifest().runtimeLibrarySelection();

        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        //when
        Result result = nativeTask.execute(context);

        //then
        assertNotNull(nativeExtractionService.getNativeExtractionPlan());
        assertFalse(nativeExtractionService.getNativeExtractionPlan().isEmpty());
        assertInstanceOf(SuccessResult.class, result);
    }

    @Test
    void should_return_success_when_native_artifacts_are_empty() {
        //given
        ExtractNativesTask nativeTask = new ExtractNativesTask(
                nativeExtractionService,
                nativeExtractionPlanBuilder
        );

        RuntimeLibrarySelection runtimeLibrarySelection = manifestService.
                loadManifestWithEmptyLibraries().runtimeLibrarySelection();

        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        //when
        Result result = nativeTask.execute(context);

        //then
        assertNull(nativeExtractionService.getNativeExtractionPlan());
        assertInstanceOf(SuccessResult.class, result);
    }

}
