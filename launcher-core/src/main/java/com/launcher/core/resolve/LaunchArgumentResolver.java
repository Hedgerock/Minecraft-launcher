package com.launcher.core.resolve;

import com.launcher.core.resolve.model.LaunchVariables;

import java.util.List;

public interface LaunchArgumentResolver {

    List<String> resolve(List<String> arguments, LaunchVariables variables);

}
