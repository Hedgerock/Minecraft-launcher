package com.launcher.api.manifest.support;

public final class ManifestJsonProvider {

    private ManifestJsonProvider() {
    }

    public static String getMinimumValidManifestJson(String version) {
        return """
                {
                    "minecraftVersion": "%s",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                        "gameArgs": ["--version", "${version_name}"],
                        "classpath": ["versions/client.jar"],
                        "javaExecutable": "java",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 17
                        }
                    },
                    "libraries": [
                        {
                            "path": "libraries/org/example/example.jar",
                            "sha256": "library-sha256",
                            "size": 123,
                            "url": "https://example.com/libraries/org/example/example.jar"
                        }
                    ]
                }
                """.formatted(version);
    }

    public static String getMinimumValidManifestJson() {
        return getMinimumValidManifestJson("1.12.2");
    }
}
