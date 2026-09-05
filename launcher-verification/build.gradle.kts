plugins {
    id("java")
}

dependencies {
    implementation(project(":launcher-model"))
    implementation(project(":launcher-storage"))
    implementation(project(":launcher-core"))
}
