package com.launcher.core.resolve;

import com.launcher.core.resolve.model.LaunchVariables;

import java.util.List;
import java.util.Objects;

public final class DefaultLaunchArgumentResolver implements LaunchArgumentResolver {

    @Override
    public List<String> resolve(List<String> arguments, LaunchVariables variables) {
        Objects.requireNonNull(arguments, "arguments");
        Objects.requireNonNull(variables, "variables");

        return arguments.stream()
                .map(argument -> resolve(argument, variables))
                .toList();
    }

    private String resolve(String argument, LaunchVariables variables) {
        if (argument.contains("${assets_index_name}")) {
            String assetsIndexName = variables.assetsIndexName();

            if (assetsIndexName == null) {
                throw new IllegalArgumentException(
                        "Asset index name is required for ${assets_index_name}"
                );
            }

            argument = argument.replace("${assets_index_name}", variables.assetsIndexName());
        }

        return argument
            .replace("${version_name}", variables.versionName())
            .replace("${game_directory}", variables.gameDirectory().toString())
            .replace("${classpath}", variables.classpath())
            .replace("${natives_directory}", variables.nativesDirectory().toString())
            .replace("${assets_root}", variables.assetsRoot().toString());
    }

}
