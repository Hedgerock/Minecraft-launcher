package com.launcher.core.operation.impl;

import com.launcher.core.directory.PrepareDirectoriesTask;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class PrepareDirectoriesOperation extends LaunchOperation {

    private final DirectoryService directoryService;


    public PrepareDirectoriesOperation(
            LaunchContext launchContext,
            ExecutionStrategy executionStrategy,
            EventBus eventBus,
            DirectoryService directoryService
    ) {
        super(
                launchContext,
                executionStrategy,
                OperationType.PREPARE_DIRECTORIES,
                eventBus
        );
        this.directoryService = directoryService;
    }

    @Override
    protected List<LauncherTask> createTask() {
        return List.of(
                new PrepareDirectoriesTask(directoryService)
        );
    }
}
