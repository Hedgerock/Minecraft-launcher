package com.launcher.storage.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalWriteTargetResolver {

    public Path prepareDirectoryTarget(
            Path trustedRoot,
            Path targetDirectory
    ) throws IOException {
        Objects.requireNonNull(trustedRoot, "trustedRoot");
        Objects.requireNonNull(targetDirectory, "targetDirectory");

        Path root = trustedRoot.toAbsolutePath().normalize();
        Path target = targetDirectory.toAbsolutePath().normalize();

        if (!target.startsWith(root)) {
            throw new IOException(
                    "Target directory escapes trusted root"
            );
        }

        Files.createDirectories(root);

        Path realRoot = root.toRealPath();
        Path current = realRoot;

        for (Path component : root.relativize(target)) {
            current = getRealNext(current, component, realRoot);
        }

        return current;
    }

    public Path prepareFileTarget(Path trustedRoot, Path targetPath) throws IOException {
        Objects.requireNonNull(trustedRoot, "trustedRoot");
        Objects.requireNonNull(targetPath, "targetPath");

        Path root = trustedRoot.toAbsolutePath().normalize();
        Path target = targetPath.toAbsolutePath().normalize();

        if (!target.startsWith(root) || target.equals(root)) {
            throw new IOException("Target path escapes trusted root");
        }

        Path preparedParent = prepareDirectoryTarget(root, target.getParent());

        return preparedParent.resolve(target.getFileName());
    }

    private Path getRealNext(Path current, Path component, Path realRoot) throws IOException {
        Path next = current.resolve(component);

        if (Files.notExists(next, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(next);
        }

        Path realNext = next.toRealPath();

        if (!realNext.startsWith(realRoot) || !Files.isDirectory(realNext)) {
            throw new IOException("Target directory escapes trusted root");
        }

        return realNext;
    }
}
