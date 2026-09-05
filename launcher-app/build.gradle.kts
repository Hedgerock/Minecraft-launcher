plugins {
    id("java")
}

dependencies {
    implementation(project(":launcher-core"))
    implementation(project(":launcher-api"))
    implementation(project(":launcher-storage"))
    implementation(project(":launcher-verification"))
    implementation(project(":launcher-downloader"))
    implementation(project(":launcher-game"))
    implementation(project(":launcher-natives"))
}
