package com.launcher.core.game.builder;

import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;

import java.util.List;

public interface GameLaunchCommandBuilder {

    List<String> build(LaunchInfo launchInfo, LaunchVariables launchVariables);

}
