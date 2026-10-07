plugins {
    id("java")
}

dependencies {
    implementation(project(":launcher-core"))
    implementation(project(":launcher-model"))
    implementation(project(":launcher-storage"))
}
