plugins {
    id("java")
}

dependencies {
    implementation(project(":launcher-common"))
    implementation(project(":launcher-model"))
    implementation(project(":launcher-auth"))

    testImplementation(project(":launcher-architecture-test-support"))
}
