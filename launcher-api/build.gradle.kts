plugins {
    id("java")
}

group = "com.hedgerock"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":launcher-common"))
    implementation(project(":launcher-model"))
    implementation(project(":launcher-auth"))
    implementation(project(":launcher-core"))

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.1"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}

tasks.test {
    useJUnitPlatform()
}