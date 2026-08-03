package com.launcher.app.service.factory;

import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.service.LauncherServices;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.directory.LocalDirectoryProvider;
import com.launcher.core.storage.service.DefaultDirectoryService;
import com.launcher.core.storage.service.DirectoryService;

public class DefaultLauncherServiceFactory implements LauncherServicesFactory {
    private final LauncherConfiguration configuration;
    private final LauncherInfrastructure infrastructure;

    public DefaultLauncherServiceFactory(LauncherConfiguration configuration, LauncherInfrastructure infrastructure) {
        this.configuration = configuration;
        this.infrastructure = infrastructure;
    }

    private ManifestService createManifestService() {
        ManifestClient manifestClient = new HttpManifestClient(
                infrastructure.httpClient(),
                configuration.manifestUri()
        );

        ManifestMapper manifestMapper = new JsonManifestMapper();
        return new HttpManifestService(
                manifestClient,
                manifestMapper
        );
    }

    private DirectoryService createDirectoryService() {
        DirectoryProvider directoryProvider = new LocalDirectoryProvider(configuration);
        return new DefaultDirectoryService(
                directoryProvider,
                infrastructure.fileStorage()
        );
    }

    @Override
    public LauncherServices createServices() {

        return new LauncherServices(
                createManifestService(),
                createDirectoryService()
        );
    }
}
