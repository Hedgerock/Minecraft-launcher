package com.launcher.core.operation.factory;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.LoadManifestOperation;
import com.launcher.core.operation.impl.RepairOperation;
import com.launcher.core.operation.type.OperationType;

public class DefaultOperationFactory implements OperationFactory {
    private final ManifestService service;
    private final EventBus eventBus;

    public DefaultOperationFactory(ManifestService service, EventBus eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public LaunchOperation create(OperationType type, LaunchContext context, ExecutionStrategy executionStrategy) {
        return switch (type) {
            case REPAIR -> new RepairOperation(context, executionStrategy, eventBus);
            case LOAD_MANIFEST -> new LoadManifestOperation(context, executionStrategy, eventBus, service);
        };
    }
}
