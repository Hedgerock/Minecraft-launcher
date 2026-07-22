package com.launcher.core.configuration;

import java.net.URI;
import java.nio.file.Path;

public record LauncherConfiguration(URI manifestUri, Path launcherDirectory) {
}
