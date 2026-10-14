plugins {
    id("java")
}

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.1"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
