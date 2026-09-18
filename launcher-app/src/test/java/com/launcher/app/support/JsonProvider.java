package com.launcher.app.support;

public final class JsonProvider {

    private JsonProvider() {
    }

    public static final String MANIFEST_JSON = getManifestJson().formatted("[]", "[]");

    public static String getManifestJsonWithSameFileAndAsset(
       String resourceUrl,
       String sha256,
       long size
    ) {

        String files = getResourceTemplate().formatted(
                "mods/example.jar", sha256, size, resourceUrl
        );

        String assets = getResourceTemplate().formatted(
                "mods/./example.jar", sha256, size, resourceUrl
        );

        return getManifestJson().formatted(files, assets);
    }

    private static String getResourceTemplate() {
        return
                """
                [
                    {
                          "path": "%s",
                          "sha256": "%s",
                          "size": %d,
                          "url": "%s"
                    }
                ]
                """;
    }

    private static String getManifestJson() {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": %s,
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
                    "libraries": [],
                    "assets": %s
                }
                """;
    }

    public static String getManifestJsonWithSupportedPlaceholdersInAuthArgs() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": [],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    },
                    "authArgs": ["--accessToken", "${access_token}", "--version", "${version_name}"]
                }
                """;

        return getModifiedManifestJson(launchInfoJson);
    }

    public static String getManifestJsonWithoutAuthArgs() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": ["--version", "${version_name}"],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    }
                }
                """;

        return getModifiedManifestJson(launchInfoJson);
    }

    public static String getModifiedManifestJson() {
        String launchInfoJson = """
                "launchInfo": {
                    "mainClass": "net.minecraft.client.main.Main",
                    "jvmArgs": ["-Xmx2G", "-Djava.class.path=${classpath}"],
                    "gameArgs": ["--version", "${version_name}"],
                    "classpath": ["versions/client.jar"],
                    "javaExecutable": "java",
                    "javaVersionRequirement": {
                        "minimumMajorVersion": 17
                    },
                    "authArgs": ["--accessToken", "${access_token}"]
                }
                """;

        return getModifiedManifestJson(launchInfoJson);
    }

    public static String getModifiedManifestJson(String currentLaunchInfoJson) {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    %s,
                    "libraries": []
                }
                """.formatted(currentLaunchInfoJson);
    }
}
