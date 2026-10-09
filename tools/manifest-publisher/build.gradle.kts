plugins {
    id("application")
}

application {
    mainClass = "com.launcher.publisher.ManifestPublisherMain"
}

dependencies {
    testImplementation(project(":launcher-api"))
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.86")
    testImplementation("org.bouncycastle:bcpkix-jdk18on:1.86")
}
