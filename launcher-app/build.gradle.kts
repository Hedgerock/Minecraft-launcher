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

    implementation(project(":launcher-core"))
    implementation(project(":launcher-api"))
    implementation(project(":launcher-storage"))
    implementation(project(":launcher-verification"))
    implementation(project(":launcher-downloader"))
    implementation(project(":launcher-game"))
    implementation(project(":launcher-model"))
}

tasks.test {
    useJUnitPlatform()
}