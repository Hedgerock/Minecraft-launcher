package com.launcher.core.architecture.support.fixture;

import com.launcher.core.architecture.support.recording.*;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.verification.model.VerificationPlan;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public class OperationFactoryFixture {

    private final DefaultOperationFactory factory;

    public static final DownloadPlanBuilder builder = new DownloadPlanBuilder();
    public static final GameLaunchPlanBuilder gameLaunchBuilder = new GameLaunchPlanBuilder(
            new RecordingDirectoryProvider(),
            new DefaultGameLaunchCommandBuilder(
                    new DefaultLaunchArgumentResolver()
            ),
            new DefaultGameClasspathBuilder(
                    new SafeResourcePathResolver()
            ),
            new RecordingClasspathFormatter()
    );

    public OperationFactoryFixture() {
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        this.factory = getDefaultOperationFactory(service, eventBus);

    }

    public static LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")

                )
        );
    }

    public DefaultOperationFactory getFactory() {
        return factory;
    }

    private VerificationPlan getVerificationPlan() {
        return new VerificationPlan(
                List.of()
        );
    }

    private DefaultOperationFactory getDefaultOperationFactory(
            ManifestService manifestService,
            EventBus eventBus
    ) {
        return new DefaultOperationFactory(
                manifestService,
                new RecordVerificationService(getVerificationPlan()),
                new RecordingDownloadService(),
                new RecordingDirectoryService(),
                new RecordingGameService(),
                builder,
                gameLaunchBuilder,
                eventBus
        );
    }

}
