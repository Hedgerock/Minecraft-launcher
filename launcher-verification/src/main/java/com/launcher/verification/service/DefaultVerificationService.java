package com.launcher.verification.service;

import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.resource.model.ResourceSetPlan;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestResources;
import com.launcher.verification.file.FileVerifier;

import java.util.List;

public class DefaultVerificationService implements VerificationService {
    private final DirectoryProvider directoryProvider;
    private final FileVerifier fileVerifier;
    private final ResourceSetPlanner resourceSetPlanner;

    public DefaultVerificationService(
            DirectoryProvider directoryProvider,
            FileVerifier fileVerifier,
            ResourceSetPlanner resourceSetPlanner
    ) {
        this.directoryProvider = directoryProvider;
        this.fileVerifier = fileVerifier;
        this.resourceSetPlanner = resourceSetPlanner;
    }

    @Override
    public VerificationPlan verify(Manifest manifest) {
        ResourceSetPlan resourceSetPlan = resourceSetPlanner.plan(
                ManifestResources.from(manifest),
                directoryProvider.directories().game()
        );

        List<ResourceVerificationResult> results = resourceSetPlan.resources()
                .stream()
                .map(resource -> fileVerifier.verify(
                        resource.targetPath(),
                        resource.resource()
                ))
                .toList();

        return new VerificationPlan(results);
    }
}
