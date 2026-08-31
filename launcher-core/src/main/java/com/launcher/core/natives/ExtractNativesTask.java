package com.launcher.core.natives;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;

public final class ExtractNativesTask implements LauncherTask {
    private final NativeExtractionService extractionService;
    private final NativeExtractionPlanBuilder nativeExtractionPlanBuilder;

    public ExtractNativesTask(
            NativeExtractionService extractionService,
            NativeExtractionPlanBuilder nativeExtractionPlanBuilder
    ) {
        this.extractionService = extractionService;
        this.nativeExtractionPlanBuilder = nativeExtractionPlanBuilder;
    }

    @Override
    public LauncherState state() {
        return LauncherState.EXTRACTING_NATIVES;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        RuntimeLibrarySelection runtimeLibrarySelection = launchContext.getRuntimeLibrarySelection();

        if (runtimeLibrarySelection == null) {
            return TaskResult.failure("Runtime library selection not available");
        }

        NativeExtractionPlan extractionPlan = nativeExtractionPlanBuilder.build(runtimeLibrarySelection);

        if (!extractionPlan.isEmpty()) {
            extractionService.extract(extractionPlan);
        }

        return TaskResult.success();
    }
}
