package com.launcher.core.factory.service;

import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.infrastructure.LauncherInfrastructure;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.service.LauncherServices;
import com.launcher.storage.directory.DirectoryProvider;
import com.launcher.storage.directory.LocalDirectoryProvider;
import com.launcher.storage.service.DefaultDirectoryService;
import com.launcher.storage.service.DirectoryService;

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
