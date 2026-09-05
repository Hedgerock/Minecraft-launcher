import org.gradle.api.GradleException

plugins {
    id("java")
}

allprojects {
    group = "com.launcher"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

data class QualityViolation(
    val file: File,
    val line: Int,
    val message: String
)

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs minimal project quality checks"

    doLast {
        val violations = mutableListOf<QualityViolation>()

        val files = fileTree(rootDir) {
            include("**/*.java")
            include("**/*.md")
            include("**/*.gradle.kts")

            exclude("**/build/**")
            exclude("**/.git/**")
            exclude("**/.gradle/**")
        }.files
            .filter { it.isFile }
            .sortedBy { it.relativeTo(rootDir).invariantSeparatorsPath }

        val nonStaticWildCardImport = Regex("""^\s*import\s+(?!static\b)[\w.]+\.\*;\s*$""")
        val suppressAll = Regex("""@SuppressWarnings\s*\(\s*"all"\s*\)""")

        files.forEach { file ->
            val bytes = file.readBytes()

            if (bytes.isNotEmpty() && bytes.last() != '\n'.code.toByte()) {
                violations += QualityViolation(
                    file,
                    1,
                    "Missing final newline"
                )
            }

            file.readLines(Charsets.UTF_8).forEachIndexed { index, line ->
                val lineNumber = index + 1

                if (line.endsWith(" ") || line.endsWith("\t")) {
                    violations += QualityViolation(
                        file,
                        lineNumber,
                        "Trailing whitespace"
                    )
                }

                if (file.extension == "java") {
                    if (nonStaticWildCardImport.containsMatchIn(line)) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "Non-static wildcard import is not allowed"
                        )
                    }

                    if (suppressAll.containsMatchIn(line)) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            """@SuppressWarnings("all") is not allowed"""
                        )
                    }

                    if (line.contains("System.out.println")) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "System.out.println is not allowed"
                        )
                    }

                    if (line.contains("printStackTrace")) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "printStackTrace is not allowed"
                        )
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            val message = violations.joinToString(separator = System.lineSeparator()) {
                violation ->
                val relativePath = violation.file.relativeTo(rootDir).invariantSeparatorsPath
                "$relativePath:${violation.line}: ${violation.message}"
            }

            throw GradleException(
                "Quality checks failed:${System.lineSeparator()}$message"
            )
        }
    }
}

tasks.named("check") {
    dependsOn("qualityCheck")
}
