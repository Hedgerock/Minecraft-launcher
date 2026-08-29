package com.launcher.core.operation.factory;

import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.download.DownloadService;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.game.GameService;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildDownloadPlanOperation;
import com.launcher.core.operation.impl.BuildGameLaunchPlanOperation;
import com.launcher.core.operation.impl.DownloadFilesOperation;
import com.launcher.core.operation.impl.LaunchGameOperation;
import com.launcher.core.operation.impl.LoadManifestOperation;
import com.launcher.core.operation.impl.PrepareDirectoriesOperation;
import com.launcher.core.operation.impl.RepairOperation;
import com.launcher.core.operation.impl.VerificationOperation;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.core.verification.VerificationService;

public class DefaultOperationFactory implements OperationFactory {
    private final ManifestService manifestService;
    private final VerificationService verificationService;
    private final DownloadService downloadService;
    private final EventBus eventBus;
    private final DownloadPlanBuilder downloadPlanBuilder;
    private final GameLaunchPlanBuilder gameLaunchPlanBuilder;
    private final DirectoryService directoryService;
    private final GameService gameService;

    public DefaultOperationFactory(
            ManifestService manifestService,
            VerificationService verificationService,
            DownloadService downloadService,
            DirectoryService directoryService,
            GameService gameService,
            DownloadPlanBuilder downloadPlanBuilder,
            GameLaunchPlanBuilder gameLaunchPlanBuilder,
            EventBus eventBus
    ) {
        this.manifestService = manifestService;
        this.verificationService = verificationService;
        this.downloadService = downloadService;
        this.directoryService = directoryService;
        this.downloadPlanBuilder = downloadPlanBuilder;
        this.gameLaunchPlanBuilder = gameLaunchPlanBuilder;
        this.eventBus = eventBus;
        this.gameService = gameService;
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
                    new LoadManifestOperation(context, executionStrategy, eventBus, manifestService);
            case VERIFY_FILES ->
                    new VerificationOperation(context, executionStrategy, eventBus, verificationService);
            case BUILD_DOWNLOAD_PLAN ->
                    new BuildDownloadPlanOperation(context, executionStrategy, eventBus, downloadPlanBuilder);
            case DOWNLOAD_FILES ->
                    new DownloadFilesOperation(context, executionStrategy, eventBus, downloadService);
            case PREPARE_DIRECTORIES ->
                    new PrepareDirectoriesOperation(context, executionStrategy, eventBus, directoryService);
            case BUILD_GAME_LAUNCH_PLAN ->
                    new BuildGameLaunchPlanOperation(context, executionStrategy, eventBus, gameLaunchPlanBuilder);
            case LAUNCH_GAME ->
                    new LaunchGameOperation(context, executionStrategy, eventBus, gameService);
        };
    }
}
