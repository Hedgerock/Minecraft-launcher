package com.launcher.build.quality

import com.sun.source.tree.CatchTree
import com.sun.source.util.JavacTask
import com.sun.source.util.TreeScanner
import com.sun.source.util.Trees
import java.io.StringWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

class EmptyCatchBlockCheck {

    fun findViolationLines(source: String): List<Int> {
        val compiler = checkNotNull(ToolProvider.getSystemJavaCompiler()) {
            "Java compiler is not available. Run Gradle with a full JDK."
        }

        val diagnostics = DiagnosticCollector<JavaFileObject>()

        return compiler.getStandardFileManager(
            diagnostics,
            Locale.ROOT,
            StandardCharsets.UTF_8
        ).use { fileManager ->
            val task = compiler.getTask(
                StringWriter(),
                fileManager,
                diagnostics,
                listOf("-proc:none", "--release", "21"),
                null,
                listOf(SourceFile(source))
            ) as JavacTask

            val units = task.parse().toList()

            val errors = diagnostics.diagnostics.filter { it.kind == Diagnostic.Kind.ERROR }

            require(errors.isEmpty()) {
                val details = errors.joinToString("; ") {
                    "line ${it.lineNumber}: ${it.getMessage(Locale.ROOT)}"
                }

                "Cannot parse source: $details"
            }

            val positions = Trees.instance(task).sourcePositions
            val violationLines = mutableListOf<Int>()

            units.forEach { unit ->
                val scanner = object : TreeScanner<Void?, Void?>() {

                    override fun visitCatch(
                        node: CatchTree,
                        unused: Void?
                    ): Void? {
                        if (node.block.statements.isEmpty()) {
                            val position = positions.getStartPosition(unit, node)
                            check(position >= 0) {
                                "Cannot determine catch block position"
                            }

                            violationLines += unit.lineMap.getLineNumber(position).toInt()
                        }

                        return super.visitCatch(node, unused)
                    }
                }

                scanner.scan(unit, null)
            }

            violationLines.toList()
        }
    }

    private class SourceFile(
        private val source: String
    ) : SimpleJavaFileObject(
        URI.create("string:///QualityCheckSource.java"),
        JavaFileObject.Kind.SOURCE
    ) {

        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
            return source
        }
    }
}
