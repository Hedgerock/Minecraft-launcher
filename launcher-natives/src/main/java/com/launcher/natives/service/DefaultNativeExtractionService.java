package com.launcher.natives.service;

import com.launcher.core.natives.NativeExtractionService;
import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.natives.NativeExtractionRules;
import com.launcher.model.manifest.natives.SelectedNativeArtifact;
import com.launcher.natives.exception.NativeExtractionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class DefaultNativeExtractionService implements NativeExtractionService {
    private final DirectoryProvider directoryProvider;
    private final ResourcePathResolver resourcePathResolver;

    public DefaultNativeExtractionService(
            DirectoryProvider directoryProvider,
            ResourcePathResolver resourcePathResolver
    ) {
        this.directoryProvider = Objects.requireNonNull(directoryProvider, "directoryProvider");
        this.resourcePathResolver = Objects.requireNonNull(resourcePathResolver, "resourcePathResolver");
    }

    @Override
    public void extract(NativeExtractionPlan plan) {
        Objects.requireNonNull(plan, "plan");

        Path gameDirectory = directoryProvider.directories().game();

        for (SelectedNativeArtifact selectedNativeArtifact : plan.artifacts()) {
            Path sourcePath = resourcePathResolver.resolve(gameDirectory, selectedNativeArtifact.artifact().path());
            extractArchive(
                    sourcePath,
                    plan.targetDirectory(),
                    selectedNativeArtifact.extractionRules()
            );
        }
    }

    private void extractArchive(
            Path sourcePath,
            Path targetDirectory,
            NativeExtractionRules extractionRules
    ) {
        try (
                InputStream inputStream = Files.newInputStream(sourcePath);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)
        ) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                extractEntry(
                        zipInputStream,
                        zipEntry,
                        targetDirectory,
                        extractionRules
                );

                zipInputStream.closeEntry();
            }

        } catch (IOException e) {
            throw new NativeExtractionException(
                    "Failed to extract native artifact",
                    e
            );
        }
    }

    private void extractEntry(
            ZipInputStream zipInputStream,
            ZipEntry zipEntry,
            Path targetDirectory,
            NativeExtractionRules extractionRules
    ) throws IOException {
        Path targetPath = resolveTargetPath(targetDirectory, zipEntry.getName());

        if (shouldExclude(zipEntry.getName(), extractionRules)) {
            return;
        }

        if (zipEntry.isDirectory()) {
            Files.createDirectories(targetPath);
            return;
        }

        Path parent = targetPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.copy(zipInputStream, targetPath);
    }

    private boolean shouldExclude(
            String entryName,
            NativeExtractionRules extractionRules
    ) {
        String normalizedEntryName = entryName.replace('\\', '/');

        return extractionRules.excludes().stream()
                .anyMatch(normalizedEntryName::startsWith);
    }

    private Path resolveTargetPath(Path targetDirectory, String entryName) {
        Path normalizedTargetDirectory = targetDirectory.normalize();
        Path targetPath = normalizedTargetDirectory.resolve(entryName).normalize();

        if (!targetPath.startsWith(normalizedTargetDirectory)) {
            throw new NativeExtractionException(
                    "Native archive entry escapes target directory"
            );
        }

        return targetPath;
    }
}
