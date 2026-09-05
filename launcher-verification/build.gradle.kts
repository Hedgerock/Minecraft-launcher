plugins {
    id("java")
}

group = "com.launcher"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":launcher-model"))
    implementation(project(":launcher-storage"))
    implementation(project(":launcher-core"))
}

tasks.test {
    useJUnitPlatform()
}
