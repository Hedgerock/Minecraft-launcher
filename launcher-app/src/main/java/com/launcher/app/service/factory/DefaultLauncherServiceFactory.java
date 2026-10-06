package com.launcher.app.service.factory;

import com.launcher.api.manifest.client.HttpManifestClient;
import com.launcher.api.manifest.client.HttpManifestSignatureClient;
import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.JsonManifestMapper;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.api.manifest.service.HttpManifestService;
import com.launcher.api.manifest.service.SignedHttpManifestService;
import com.launcher.api.manifest.signature.Ed25519ManifestSignatureVerifier;
import com.launcher.app.configuration.BundledManifestPublicKeySource;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.service.LauncherServices;
import com.launcher.core.download.DownloadService;
import com.launcher.core.game.GameService;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.natives.NativeExtractionService;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.runtime.RuntimeEnvironmentProvider;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.service.DefaultDirectoryService;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.core.verification.VerificationService;
import com.launcher.downloader.download.DefaultFileDownloader;
import com.launcher.downloader.download.FileDownloader;
import com.launcher.downloader.service.DefaultDownloadService;
import com.launcher.game.process.ProcessBuilderGameProcessLauncher;
import com.launcher.game.service.DefaultGameService;
import com.launcher.natives.service.DefaultNativeExtractionService;
import com.launcher.storage.file.FileMetadataReader;
import com.launcher.storage.file.LocalFileMetadataReader;
import com.launcher.storage.hash.HashService;
import com.launcher.storage.hash.Sha256HashService;
import com.launcher.verification.file.DefaultFileVerifier;
import com.launcher.verification.file.FileVerifier;
import com.launcher.verification.service.DefaultVerificationService;

import java.security.PublicKey;
import java.util.Objects;
import java.util.function.Supplier;

public final class DefaultLauncherServiceFactory implements LauncherServicesFactory {
    private final ResolvedLauncherConfiguration resolvedLauncherConfiguration;
    private final LauncherInfrastructure infrastructure;
    private final ResourcePathResolver resourcePathResolver;
    private final DirectoryProvider directoryProvider;
    private final RuntimeEnvironmentProvider environmentProvider;
    private final Supplier<PublicKey> publicKeySupplier;

    DefaultLauncherServiceFactory(
            ResolvedLauncherConfiguration resolvedLauncherConfiguration,
            LauncherInfrastructure infrastructure,
            ResourcePathResolver resourcePathResolver,
            DirectoryProvider directoryProvider,
            RuntimeEnvironmentProvider environmentProvider,
            Supplier<PublicKey> publicKeySupplier
    ) {
        this.resolvedLauncherConfiguration = Objects.requireNonNull(
                resolvedLauncherConfiguration,
                "resolvedLauncherConfiguration"
        );
        this.infrastructure = Objects.requireNonNull(
                infrastructure,
                "infrastructure"
        );
        this.resourcePathResolver = Objects.requireNonNull(
                resourcePathResolver,
                "resourcePathResolver"
        );
        this.directoryProvider = Objects.requireNonNull(
                directoryProvider,
                "directoryProvider"
        );
        this.environmentProvider = Objects.requireNonNull(
                environmentProvider,
                "environmentProvider"
        );
        this.publicKeySupplier = Objects.requireNonNull(
                publicKeySupplier,
                "publicKeySupplier"
        );
    }

    public DefaultLauncherServiceFactory(
            ResolvedLauncherConfiguration resolvedLauncherConfiguration,
            LauncherInfrastructure infrastructure,
            ResourcePathResolver resourcePathResolver,
            DirectoryProvider directoryProvider,
            RuntimeEnvironmentProvider environmentProvider
    ) {
        this(
                resolvedLauncherConfiguration,
                infrastructure,
                resourcePathResolver,
                directoryProvider,
                environmentProvider,
                () -> {
                    BundledManifestPublicKeySource source =
                            new BundledManifestPublicKeySource();
                    return source.load();
                }
        );
    }

    @Override
    public LauncherServices createServices() {
        ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(resourcePathResolver);

        return new LauncherServices(
                createManifestService(),
                createVerificationService(directoryProvider, resourceSetPlanner),
                createDirectoryService(directoryProvider),
                createDownloadService(directoryProvider, resourceSetPlanner),
                createGameService(),
                createNativeExtractionService(directoryProvider, resourcePathResolver)
        );
    }

    private ManifestService createManifestService() {
        ManifestClient manifestClient = new HttpManifestClient(
                infrastructure.launcherHttpClient(),
                resolvedLauncherConfiguration.configuration().manifestUri()
        );
        RuntimeLibrarySelector runtimeLibrarySelector = new DefaultRuntimeLibrarySelector();

        ManifestMapper manifestMapper =
                new JsonManifestMapper(
                        runtimeLibrarySelector,
                        environmentProvider
                );

        return switch (resolvedLauncherConfiguration.sourceKind()) {
            case EXPLICIT_URI, LOCAL_CONFIG ->
                    new HttpManifestService(
                            manifestClient,
                            manifestMapper
                    );
            case MANAGED -> {
                PublicKey publicKey = Objects.requireNonNull(publicKeySupplier.get(), "publicKey");

                yield new SignedHttpManifestService(
                        manifestClient,
                        new HttpManifestSignatureClient(
                                infrastructure.launcherHttpClient(),
                                resolvedLauncherConfiguration.managedManifestUris().orElseThrow().signatureUri()
                        ),
                        new Ed25519ManifestSignatureVerifier(publicKey),
                        manifestMapper
                );
            }
        };
    }

    private DirectoryService createDirectoryService(DirectoryProvider directoryProvider) {
        return new DefaultDirectoryService(
                directoryProvider,
                infrastructure.fileStorage()
        );
    }

    private DownloadService createDownloadService(
            DirectoryProvider directoryProvider,
            ResourceSetPlanner resourceSetPlanner
    ) {
        FileDownloader downloader = switch (resolvedLauncherConfiguration.sourceKind()) {
            case MANAGED -> DefaultFileDownloader.forManagedResources();
            case EXPLICIT_URI, LOCAL_CONFIG -> new DefaultFileDownloader();
        };

        return new DefaultDownloadService(directoryProvider, downloader, resourceSetPlanner);
    }

    private VerificationService createVerificationService(
            DirectoryProvider directoryProvider,
            ResourceSetPlanner resourceSetPlanner
    ) {
        FileMetadataReader metadataReader = new LocalFileMetadataReader();
        HashService hashService = new Sha256HashService();
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        return new DefaultVerificationService(directoryProvider, fileVerifier, resourceSetPlanner);
    }

    private NativeExtractionService createNativeExtractionService(
            DirectoryProvider directoryProvider,
            ResourcePathResolver resourcePathResolver
    ) {
        return new DefaultNativeExtractionService(
                directoryProvider,
                resourcePathResolver
        );
    }

    private GameService createGameService() {
        return new DefaultGameService(
                new ProcessBuilderGameProcessLauncher()
        );
    }
}
