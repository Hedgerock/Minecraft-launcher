import java.io.File
import java.nio.file.Files
import org.gradle.api.GradleException

data class QualityViolation(
    val file: File,
    val line: Int,
    val message: String
)

data class MarkdownLink(
    val line: Int,
    val target: String
)

fun maskInlineCode(line: String): String {
    val result = line.toCharArray()
    var index = 0
    val expected = '`'

    while (index < line.length) {
        if (line[index] != expected) {
            index++
            continue
        }

        val start = index

        while (index < line.length && line[index] == expected) {
            index++
        }

        val delimiterLength = index - start
        var cursor = index
        var closingEnd: Int? = null

        while (cursor < line.length) {
            if (line[cursor] != expected) {
                cursor++
                continue
            }

            val closingStart = cursor

            while (cursor < line.length && line[cursor] == expected) {
                cursor++
            }

            if (cursor - closingStart == delimiterLength) {
                closingEnd = cursor
                break
            }
        }

        if (closingEnd != null) {
            for (position in start until closingEnd) {
                result[position] = ' '
            }

            index = closingEnd
        }
    }

    return String(result)
}

fun extractMarkdownLinks(lines: List<String>): List<MarkdownLink> {
    val links = mutableListOf<MarkdownLink>()

    val fencePattern = Regex("""^ {0,3}(`{3,}|~{3,})(.*)$""")

    val inlineLinkPattern = Regex(
        """\[[^]]*]\(\s*(<[^>\n]+>|[^\s()]+)(?:\s+(?:"[^"]*"|'[^']*'|\([^)]*\)))?\s*\)"""
    )

    val referenceDefinitionPattern = Regex(
        """^ {0,3}\[[^]]+]:\s*(<[^>\n]+>|[^\s]+)(?:\s+(?:"[^"]*"|'[^']*'|\([^)]*\)))?\s*$"""
    )

    var fenceCharacter: Char? = null
    var fenceLength = 0

    lines.forEachIndexed { index, line ->
        val activeFence = fenceCharacter

        if (activeFence != null) {
            val closingPattern = Regex(
                "^ {0,3}${Regex.escape(activeFence.toString())}" +
                        "{$fenceLength,}[ \\t]*$"
            )

            if (closingPattern.matches(line)) {
                fenceCharacter = null
                fenceLength = 0
            }
        } else {
            val openingFence = fencePattern.matchEntire(line)

            if (openingFence != null) {
                val delimiter = openingFence.groupValues[1]
                val info = openingFence.groupValues[2]

                if (delimiter.first() != '`' || !info.contains('`')) {
                    fenceCharacter = delimiter.first()
                    fenceLength = delimiter.length

                    return@forEachIndexed
                }
            }

            val referenceDefinition = referenceDefinitionPattern.matchEntire(line)

            if (referenceDefinition != null) {
                links += MarkdownLink(
                    line = index + 1,
                    target = referenceDefinition.groupValues[1]
                        .removeSurrounding("<", ">")
                )
            } else {
                val searchableLine = maskInlineCode(line)

                inlineLinkPattern.findAll(searchableLine).forEach { match ->
                    links += MarkdownLink(
                        line = index + 1,
                        target = match.groupValues[1]
                            .removeSurrounding("<", ">")
                    )
                }
            }
        }
    }

    return links
}

fun validateLocalMarkdownLink(
    source: File,
    link: MarkdownLink
): QualityViolation? {
    val target = link.target.trim()

    val hasScheme = Regex("""^[A-Za-z][A-Za-z0-9+.-]*:""")

    if (
        target.startsWith("#") ||
        target.startsWith("//") ||
        hasScheme.containsMatchIn(target)
    ) {
        return null
    }

    val pathPart = target
        .substringBefore('#')
        .substringBefore('?')

    if (pathPart.isEmpty()) {
        return null
    }

    val resolvedPath = source.parentFile.toPath()
        .resolve(pathPart)
        .normalize()

    if (Files.exists(resolvedPath)) {
        return null
    }

    return QualityViolation(
        source,
        link.line,
        "Broken local Markdown link: $target"
    )
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs minimal project quality checks"

    doLast {
        val violations = mutableListOf<QualityViolation>()

        val files = fileTree(rootDir) {
            include("**/*.java")
            include("**/*.md")
            include("**/*.gradle.kts")
            include("**/*.json")

            exclude("**/build/**")
            exclude("**/.git/**")
            exclude("**/.gradle/**")
        }.files
            .filter { it.isFile }
            .sortedBy { it.relativeTo(rootDir).invariantSeparatorsPath }

        val wildcardImport = Regex("""^\s*import\s+(?:static\s+)?[\w.]+\.\*;\s*$""")
        val suppressAll = Regex("""@SuppressWarnings\s*\(\s*"all"\s*\)""")
        val emptyCatchBlock = Regex("""catch\s*\([^)]*\)\s*\{\s*}""")

        files.forEach { file ->
            val bytes = file.readBytes()
            val lines = file.readLines(Charsets.UTF_8)

            if (bytes.isNotEmpty() && bytes.last() != '\n'.code.toByte()) {
                violations += QualityViolation(
                    file,
                    1,
                    "Missing final newline"
                )
            }

            if (file.extension == "md") {
                extractMarkdownLinks(lines).forEach { link ->
                    validateLocalMarkdownLink(file, link)?.let {
                        violations += it
                    }
                }
            }

            lines.forEachIndexed { index, line ->
                val lineNumber = index + 1

                if (line.endsWith(" ") || line.endsWith("\t")) {
                    violations += QualityViolation(
                        file,
                        lineNumber,
                        "Trailing whitespace"
                    )
                }

                if (file.extension == "java") {
                    if (wildcardImport.containsMatchIn(line)) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "Wildcard import is not allowed"
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

                    if (line.contains("System.err.println")) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "System.err.println is not allowed"
                        )
                    }

                    if (emptyCatchBlock.containsMatchIn(line)) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "Empty catch block is not allowed"
                        )
                    }

                    if (line.contains("printStackTrace")) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            "printStackTrace is not allowed"
                        )
                    }

                    if (file.name.endsWith("Test.java") && line.contains("//given && when")) {
                        violations += QualityViolation(
                            file,
                            lineNumber,
                            """Invalid combined test section. Use "//given & when" instead"""
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
