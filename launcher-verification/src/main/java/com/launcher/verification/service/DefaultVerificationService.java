package com.launcher.verification.service;

import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestResources;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.verification.file.FileVerifier;

import java.nio.file.Path;
import java.util.List;

public class DefaultVerificationService implements VerificationService {
    private final DirectoryProvider directoryProvider;
    private final FileVerifier fileVerifier;
    private final ResourcePathResolver resourcePathResolver;

    public DefaultVerificationService(
            DirectoryProvider directoryProvider,
            FileVerifier fileVerifier,
            ResourcePathResolver resourcePathResolver
    ) {
        this.directoryProvider = directoryProvider;
        this.fileVerifier = fileVerifier;
        this.resourcePathResolver = resourcePathResolver;
    }

    @Override
    public VerificationPlan verify(Manifest manifest) {
        List<ResourceVerificationResult> results = ManifestResources.from(manifest)
                .stream()
                .map(this::verifyResource)
                .toList();

        return new VerificationPlan(results);
    }

    private ResourceVerificationResult verifyResource(ResourceEntry resource) {
        Path gameDirectory = directoryProvider.directories().game();
        String resourcePath = resource.path();

        Path filePath = resourcePathResolver.resolve(gameDirectory, resourcePath);

        return fileVerifier.verify(filePath, resource);
    }
}
