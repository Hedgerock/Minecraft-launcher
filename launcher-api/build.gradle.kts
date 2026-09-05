plugins {
    id("java")
}

dependencies {
    implementation(project(":launcher-common"))
    implementation(project(":launcher-model"))
    implementation(project(":launcher-auth"))
    implementation(project(":launcher-core"))

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.1"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
