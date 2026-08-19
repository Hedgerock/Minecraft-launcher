package com.launcher.core.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;

public interface GameClasspathBuilder {

    GameClasspath build(LaunchInfo launchInfo, Path gameDirectory);

}
