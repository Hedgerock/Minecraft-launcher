package com.launcher.core.operation.impl;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.natives.ExtractNativesTask;
import com.launcher.core.natives.NativeExtractionPlanBuilder;
import com.launcher.core.natives.NativeExtractionService;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class ExtractNativesOperation extends LaunchOperation {
    private final NativeExtractionPlanBuilder builder;
    private final NativeExtractionService extractionService;

    public ExtractNativesOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            NativeExtractionService extractionService,
            NativeExtractionPlanBuilder builder
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.EXTRACT_NATIVES,
                eventBus
        );

        this.builder = builder;
        this.extractionService = extractionService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new ExtractNativesTask(extractionService, builder)
        );
    }
}
