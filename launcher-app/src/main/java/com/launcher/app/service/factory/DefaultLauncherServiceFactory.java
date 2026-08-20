package com.launcher.app.service.factory;

import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.service.LauncherServices;
import com.launcher.core.download.DownloadService;
import com.launcher.core.game.GameService;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.directory.LocalDirectoryProvider;
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

    public DefaultLauncherServiceFactory(LauncherConfiguration configuration, LauncherInfrastructure infrastructure) {
        this.configuration = configuration;
        this.infrastructure = infrastructure;
    }

    private ManifestService createManifestService() {
        ManifestClient manifestClient = new HttpManifestClient(
                infrastructure.launcherHttpClient(),
                configuration.manifestUri()
        );

        ManifestMapper manifestMapper = new JsonManifestMapper();
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

    private DownloadService createDownloadService(DirectoryProvider directoryProvider) {
        FileDownloader downloader = new DefaultFileDownloader();
        return new DefaultDownloadService(directoryProvider, downloader);
    }

    private VerificationService createVerificationService(DirectoryProvider directoryProvider) {
        FileMetadataReader metadataReader = new LocalFileMetadataReader();
        HashService hashService = new Sha256HashService();

        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);


        return new DefaultVerificationService(directoryProvider, fileVerifier);
    }

    private GameService createGameService() {
        return new DefaultGameService(
                new ProcessBuilderGameProcessLauncher()
        );
    }

    @Override
    public LauncherServices createServices() {
        DirectoryProvider directoryProvider = new LocalDirectoryProvider(configuration);


        return new LauncherServices(
                createManifestService(),
                createVerificationService(directoryProvider),
                createDirectoryService(directoryProvider),
                createDownloadService(directoryProvider),
                createGameService(),
                directoryProvider
        );
    }
}
