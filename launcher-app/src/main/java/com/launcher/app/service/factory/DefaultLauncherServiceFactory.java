package com.launcher.app.service.factory;

import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.service.LauncherServices;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadService;
import com.launcher.core.game.GameService;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.service.DefaultDirectoryService;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.core.verification.VerificationService;
import com.launcher.downloader.download.DefaultFileDownloader;
import com.launcher.downloader.download.FileDownloader;
import com.launcher.downloader.service.DefaultDownloadService;
import com.launcher.game.process.ProcessBuilderGameProcessLauncher;
import com.launcher.game.service.DefaultGameService;
import com.launcher.storage.file.FileMetadataReader;
import com.launcher.storage.file.LocalFileMetadataReader;
import com.launcher.storage.hash.HashService;
import com.launcher.storage.hash.Sha256HashService;
import com.launcher.verification.file.DefaultFileVerifier;
import com.launcher.verification.file.FileVerifier;
import com.launcher.verification.service.DefaultVerificationService;

public class DefaultLauncherServiceFactory implements LauncherServicesFactory {
    private final LauncherConfiguration configuration;
    private final LauncherInfrastructure infrastructure;
    private final ResourcePathResolver resourcePathResolver;
    private final DirectoryProvider directoryProvider;

    public DefaultLauncherServiceFactory(
            LauncherConfiguration configuration,
            LauncherInfrastructure infrastructure,
            ResourcePathResolver resourcePathResolver,
            DirectoryProvider directoryProvider
    ) {
        this.configuration = configuration;
        this.infrastructure = infrastructure;
        this.resourcePathResolver = resourcePathResolver;
        this.directoryProvider = directoryProvider;
    }

    private ManifestService createManifestService() {
        ManifestClient manifestClient = new HttpManifestClient(
                infrastructure.launcherHttpClient(),
                configuration.manifestUri()
        );
        RuntimeLibrarySelector runtimeLibrarySelector = new DefaultRuntimeLibrarySelector();
        ManifestMapper manifestMapper = new JsonManifestMapper(runtimeLibrarySelector);
        return new HttpManifestService(
                manifestClient,
                manifestMapper
        );
    }

    private DirectoryService createDirectoryService(DirectoryProvider directoryProvider) {
        return new DefaultDirectoryService(
                directoryProvider,
                infrastructure.fileStorage()
        );
    }

    private DownloadService createDownloadService(
            DirectoryProvider directoryProvider,
            ResourcePathResolver resourcePathResolver
    ) {
        FileDownloader downloader = new DefaultFileDownloader();
        return new DefaultDownloadService(directoryProvider, downloader, resourcePathResolver);
    }

    private VerificationService createVerificationService(
            DirectoryProvider directoryProvider,
            ResourcePathResolver resourcePathResolver
    ) {
        FileMetadataReader metadataReader = new LocalFileMetadataReader();
        HashService hashService = new Sha256HashService();
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        return new DefaultVerificationService(directoryProvider, fileVerifier, resourcePathResolver);
    }

    private GameService createGameService() {
        return new DefaultGameService(
                new ProcessBuilderGameProcessLauncher()
        );
    }

    @Override
    public LauncherServices createServices() {

        return new LauncherServices(
                createManifestService(),
                createVerificationService(directoryProvider, resourcePathResolver),
                createDirectoryService(directoryProvider),
                createDownloadService(directoryProvider, resourcePathResolver),
                createGameService()
        );
    }
}
