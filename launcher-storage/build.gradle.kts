plugins {
    id("java")
    id("java-test-fixtures")
}

dependencies {
    implementation(project(":launcher-core"))
    implementation(project(":launcher-model"))
}
