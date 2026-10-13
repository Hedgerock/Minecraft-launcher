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

    public static String getManifestJsonRequiringAssetIndexWithoutDeclaration() {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": [],
                        "gameArgs": ["--assetsDir", "${assets_root}", "--assetIndex", "${assets_index_name}"],
                        "classpath": ["versions/client.jar"],
                        "javaExecutable": "java",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 17
                        }
                    },
                    "libraries": []
                }
                """;

    }

    public static String getManifestJsonWithGameArgsAndAssetIndex() {
        return """
                {
                    "minecraftVersion": "1.12.2",
                    "loader": {
                        "type": "fabric",
                        "version": "0.16.10"
                    },
                    "files": [],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": [],
                        "gameArgs": ["--assetsDir", "${assets_root}", "--assetIndex", "${assets_index_name}"],
                        "classpath": ["versions/client.jar"],
                        "javaExecutable": "java",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 17
                        }
                    },
                    "libraries": [],
                    "assetIndex": {
                        "id": "17",
                        "path": "assets/indexes/17.json",
                        "sha256": "<sha256-value>",
                        "size": 123,
                        "url": "https://example.test/assets/indexes/17.json"
                    }
                }
                """;

    }

    public static String getManifestJsonWithClientArtifact(
            String declaredFilePath,
            String clientArtifactPath
    ) {
        return """
                {
                    "minecraftVersion": "1.21.1",
                    "files": [
                        {
                            "path": "%s",
                            "sha256": "client-sha256",
                            "size": 1,
                            "url": "https://localhost/client.jar"
                        }
                    ],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": [
                            "-Djava.class.path=${classpath}"
                        ],
                        "gameArgs": [],
                        "classpath": [],
                        "javaExecutable": "java",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 17
                        },
                        "clientArtifactPath": "%s"
                    },
                    "libraries": [
                        {
                            "path": "libraries/example/library.jar",
                            "sha256": "library-sha256",
                            "size": 1,
                            "url": "https://localhost/library.jar",
                            "rules": [
                                {
                                    "action": "allow",
                                    "os": "windows"
                                }
                            ]
                        }
                    ]
                }
                """.formatted(declaredFilePath, clientArtifactPath);
    }

    public static String getManifestJsonWithDownloadableClientArtifact(
            String clientPath,
            String sha256,
            long size,
            String clientUrl
    ) {
        return """
                {
                    "minecraftVersion": "1.21.1",
                    "files": [
                        {
                            "path": "%s",
                            "sha256": "%s",
                            "size": %d,
                            "url": "%s"
                        }
                    ],
                    "launchInfo": {
                        "mainClass": "net.minecraft.client.main.Main",
                        "jvmArgs": ["-cp", "${classpath}"],
                        "gameArgs": [],
                        "classpath": [],
                        "javaExecutable": "java",
                        "javaVersionRequirement": {
                            "minimumMajorVersion": 21
                        },
                        "clientArtifactPath": "%s"
                    },
                    "libraries": []
                }
                """.formatted(clientPath, sha256, size, clientUrl, clientPath);
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
