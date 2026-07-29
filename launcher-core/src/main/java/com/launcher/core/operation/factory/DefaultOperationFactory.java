package com.launcher.core.operation.factory;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.LoadManifestOperation;
import com.launcher.core.operation.impl.RepairOperation;
import com.launcher.core.operation.type.OperationType;

public class DefaultOperationFactory implements OperationFactory {
    private final ManifestService service;

    public DefaultOperationFactory(ManifestService service) {
        this.service = service;
    }

    @Override
    public LaunchOperation create(OperationType type, LaunchContext context, ExecutionStrategy executionStrategy) {
        return switch (type) {
            case REPAIR -> new RepairOperation(context, executionStrategy);
            case LOAD_MANIFEST -> new LoadManifestOperation(context, executionStrategy, service);
        };
    }
}
