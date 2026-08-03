plugins {
    id("java")
}

group = "com.hedgerock"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":launcher-common"))
    implementation(project(":launcher-model"))
    implementation(project(":launcher-auth"))
}


tasks.test {
    useJUnitPlatform()
}