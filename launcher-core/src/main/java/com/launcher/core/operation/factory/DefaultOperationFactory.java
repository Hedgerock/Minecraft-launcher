package com.launcher.core.operation.factory;

import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildDownloadPlanOperation;
import com.launcher.core.operation.impl.LoadManifestOperation;
import com.launcher.core.operation.impl.RepairOperation;
import com.launcher.core.operation.impl.VerificationOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.verification.VerificationService;

public class DefaultOperationFactory implements OperationFactory {
    private final ManifestService service;
    private final VerificationService verificationService;
    private final EventBus eventBus;
    private final DownloadPlanBuilder downloadPlanBuilder;

    public DefaultOperationFactory(
            ManifestService service,
            VerificationService verificationService,
            DownloadPlanBuilder downloadPlanBuilder,
            EventBus eventBus
    ) {
        this.service = service;
        this.verificationService = verificationService;
        this.downloadPlanBuilder = downloadPlanBuilder;
        this.eventBus = eventBus;
    }

    @Override
    public LaunchOperation create(
            OperationType type,
            LaunchContext context,
            ExecutionStrategy executionStrategy
    ) {
        return switch (type) {
            case REPAIR ->
                    new RepairOperation(context, executionStrategy, eventBus);
            case LOAD_MANIFEST ->
                    new LoadManifestOperation(context, executionStrategy, eventBus, service);
            case VERIFY_FILES ->
                    new VerificationOperation(context, executionStrategy, eventBus, verificationService);
            case BUILD_DOWNLOAD_PLAN ->
                    new BuildDownloadPlanOperation(context, executionStrategy, eventBus, downloadPlanBuilder);
        };
    }
}
